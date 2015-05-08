# Code from Benedikt Boecking
# -*- coding: UTF-8 -*-
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.converter import TextConverter
from pdfminer.layout import LAParams
from pdfminer.pdfpage import PDFPage
from cStringIO import StringIO
import nltk, re, pprint,sys,os,string,glob
import multiprocessing as mp
from dateutil import parser 

#unwanted chars:
charsNOWHITE = '"#()\'*+,./<=>@[\\]^_`{|}~'
charsWHITE =',.&!+:;?'
tab1='        '
transtable=string.maketrans(charsWHITE,tab1)
transnewline=string.maketrans('\n',' ')

#init variables
p1=re.compile('order\s*of\s*the\s*Supreme\s*Court|judgment\s*of\s*the\s*Supreme\s*Court')
p2=re.compile('County\s*Court')
p3=re.compile('Family\s*Court')
p4=re.compile('Court\s*of\s*Claims')
p5=re.compile('Surrogate[s]?\s*Court') 
p6=re.compile('ORDERED')
p7 = re.compile('unanimously\s*affirmed|affirmed', re.IGNORECASE)
p8 = re.compile('unanimously\s*modified|modified', re.IGNORECASE)
p9 = re.compile('unanimously\s*reversed|reversed', re.IGNORECASE)
p10 = re.compile('unanimously\s*dismissed|dismissed', re.IGNORECASE)
p11 = re.compile('case\s*is\s*held', re.IGNORECASE)
p12 = re.compile('decision\s*is\s*reserved', re.IGNORECASE)
p13 = re.compile('matter\s*is\s*remitted', re.IGNORECASE)
DA = re.compile('([A-Z]+\s*([A-Z]\.\s*)?[A-Z]+)(,\s*[A-Z]+)?,\s*(DISTRICT\s*ATTORNEY)')
p14 = re.compile('motion\s*for', re.IGNORECASE)
p15 = re.compile('writ\s*of\s*error|reargument', re.IGNORECASE)
p16 = re.compile('granted|denied', re.IGNORECASE)
present = re.compile('PRESENT:\s*')
mode = re.compile('plea\s*of\s*guilty|jury\s*verdict|nonjury\s*trial', re.IGNORECASE)
convof = re.compile('conviction\s*of', re.IGNORECASE)
motion = re.compile('MOTION\sNO\.')
defense = re.compile('PUBLIC\s*DEFENDER|CONFLICT\s*DEFENDER|LEGAL\s*AID\s*BUREAU|LEGAL\s*AID\s*SOCIETY')
crimestopwords=['a','in','of','the','first','second','third','fourth','fifth','degree','one','two','three','four','five','six','counts','and','or']

#date
pDate=re.compile('(January|February|March|April|May|June|July|August|September|October|November|December),?(\s\d{1,2},\s\d{4})')
lastdate=re.compile('Entered:\s*(January|February|March|April|May|June|July|August|September|October|November|December)(\s\d{1,2},\s\d{4})')
sexoffender= re.compile('sex\s*offender\s*registration\s*act', re.IGNORECASE)

harm=re.compile('([^.]*?harmless[^.]*\.)')

notharm=re.compile('not\s*harmless', re.IGNORECASE)

p17=re.compile(',')
p18=re.compile('\.')
cases_to_remove=frozenset(['KA 10-01469','KA 10-00808','KA 08-01143'])
crimeset=set()

recasenumber = re.compile('[CK]A[FH]?\s\d{2}-\d{5}')

def convert_pdf_to_txt(path):
        rsrcmgr = PDFResourceManager()
        retstr = StringIO()
        codec = 'utf-8'
        laparams = LAParams()
        device = TextConverter(rsrcmgr, retstr, codec=codec, laparams=laparams)
        fp = file(path, 'rb')
        interpreter = PDFPageInterpreter(rsrcmgr, device)
        password = ""
        maxpages = 0
        caching = True
        pagenos=set()
        for page in PDFPage.get_pages(fp, pagenos, maxpages=maxpages, password=password,caching=caching, check_extractable=True):
                interpreter.process_page(page)
        fp.close()
        device.close()
        str = retstr.getvalue()
        retstr.close()
        return str

def split_docs(text):
        docs={}
        iterreg=recasenumber.finditer(text)
        first=True
        lastmatch=""
        start=0
        for match in iterreg:
                if match.group(0) == lastmatch:
                        continue
                else:
                        if first:
                                first=False
                                lastmatch=match.group(0)
                                continue
                        else:
                                if 'KA ' in lastmatch:
                                        docs[lastmatch]=text[start:match.start()]
                                start=match.start()
                                lastmatch=match.group(0)
        if 'KA ' in lastmatch:
                docs[lastmatch]=text[start:]
        return docs

def clean_text(text):
        text = filter(lambda x: x in string.printable, text)
        return text


        
def parallel_get_texts():
        pool = mp.Pool(processes=4)
        files= [filename for filename in glob.glob(os.path.join(os.getcwd()+'/pdfs/', '*.pdf'))]
        results = pool.map(convert_pdf_to_txt, files)
        out=open(outfile,'w')
        out.write('\n'.join(results))

def featurize(document,casenumber):
        try:
            pos=p6.search(document).end()
            postdoc=document[pos:]
            predoc=document[:pos]
        except AttributeError:
            postdoc=document
            predoc=document

        #DISTRICT ATTOURNEY
        distr_att=DA.search(predoc).group(1)

        #defendant appellant
        defap='0'
        defrep='0'
        if 'DEFENDANT-APPELLANT' in predoc:
            defap='1'
        elif 'DEFENDANT-RESPONDENT' in predoc:
            defrep='1'
        elif 'DEFENDANTS-RESPONDENTS' in predoc:
            defrep='1'
        elif 'DEFENDANTS-APPELLANTS' in predoc:
            defap='1'

        #get date of conviction
        this=lastdate.search(postdoc)
        enddate=(this.group(1)+this.group(2)).replace(',','')
        this=pDate.search(predoc)
        date=this.group().replace(',','')
        gap=(parser.parse(enddate) - parser.parse(date)).days


        #get mode of conviction and crime

        idx=this.end()+predoc[this.end():].find('.')
        conviction = mode.search(predoc[idx:])
        if conviction is not None:
            end = predoc[idx+conviction.end():].find('.')
            end = idx+conviction.end()+end
            crimes=predoc[idx+conviction.end():end].replace(' and ',',').split(',')
            for  i,y in enumerate(crimes):
                y=y.translate(None,charsNOWHITE).replace('\n',' ')
                crimes[i]=' '.join([x for x in y.split(' ') if x not in crimestopwords]).strip()
            crimes = filter(None, crimes)
            crimeset.update(crimes)
            crime=';'.join(set(crimes))
            conviction=conviction.group()
        else:
            conviction=convof.search(predoc[idx:])
            if conviction is not None:
                end = predoc[idx+conviction.end():].find('.')
                end = idx+conviction.end()+end
                crimes=predoc[idx+conviction.end():end].replace(' and ',',').split(',')
                for  i,y in enumerate(crimes):
                    y=y.translate(None,charsNOWHITE).replace('\n',' ')
                    crimes[i]=' '.join([x for x in y.split(' ') if x not in crimestopwords]).strip()
                crimes = filter(None, crimes)
                crimeset.update(crimes)
                crime=';'.join(set(crimes))
                conviction='NULL'
            else:
                conviction='NULL'
                if sexoffender.search(predoc[idx:]) is not None:
                    crime='risk pursuant to Sex Offender Registration Act'
                else:
                    print casenumber
                    print predoc[idx:]
                    crime='NULL'

        # get Judges 
        this=present.search(predoc)
        temp=predoc[this.end():]
        judges= temp[:temp.find('\n')].replace(',',';')

        #get defense representation
        rep=defense.search(predoc)
        if rep is None:
            rep='PRIVATE'
        else:
            rep=rep.group()

        #get court
        court='NULL'
        county='NULL'
        judge='NULL'
        m=p1.search(predoc)

        if m is not None:
            court='Supreme Court'
            end=m.end()
            county=predoc[end:].replace(',','').lstrip().split()[0]
            start=predoc[end:].find('(')
            end2=predoc[end:].find(')')
            judge=predoc[end+start+1:end+end2].replace(',',';')
        else:
            m=p2.search(predoc)
            if m is not None:
                court=m.group()
                county=predoc[:m.start()].rstrip().rsplit(' ', 1)[1]
                start=predoc[m.end():].find('(')
                end=predoc[m.end():].find(')')
                judge=predoc[m.end()+start+1:m.end()+end].replace(',',';')

        #(not) harmless errors
        #regex
        harmless=harm.findall(postdoc)
        #nonregex
        harmless=[sentence + '.' for sentence in postdoc.split('.') if 'harmless' in sentence]
        notharmless_error='0'
        harmless_error='0'
        if len(harmless)>0:
            for sentence in harmless:
                if 'error' in sentence.lower():
                    if notharm.search(sentence) is not None:
                        notharmless_error='1'
                    else:
                        harmless_error='1'

        #get orders
        orders=set()
        orders.update(p7.findall(postdoc))
        orders.update(p8.findall(postdoc))
        orders.update(p9.findall(postdoc))
        orders.update(p10.findall(postdoc))
        orders.update(p11.findall(postdoc))
        orders.update(p12.findall(postdoc))
        orders.update(p13.findall(postdoc))

        response=casenumber+','+str(len(document))+','+court+','+county+','+judge+','+';'.join(orders)+','+date+','+enddate+','+str(gap)+','+conviction+','+crime+','+judges.strip()+','+rep+','+defap+','+defrep
        response+=','+distr_att+','+harmless_error+','+notharmless_error
        
        return response.translate(transnewline)

if __name__ == '__main__':
    filename='CourtOutput.csv'
    f=open(filename,'w')
    header = 'Casenumber,DocumentLength,Court,County,Judge,Keywords,FirstDate,AppealDate,Gap(days),ModeOfConviction,Crimes,Judges,Defense,DefendantAppellant,DefendantRespondent,DistrictAttorney,HarmlessError,NotHarmlessError'
    f.write(header+'\n')
    with open ("pdftext.txt", "r") as myfile:
        text=clean_text(myfile.read())
    docs=split_docs(text)
    results=[]
    motions=[]
    toremove=[]
    for casenumber in docs:
        doc=docs[casenumber]
        if DA.search(doc) is None:
            toremove.append(casenumber)
            motions.append(doc)
            continue
        if casenumber in cases_to_remove:
            toremove.append(casenumber)
            motions.append(doc)
            continue
        if motion.search(doc):
            toremove.append(casenumber)
            motions.append(doc)
    for x in toremove:
        del docs[x]
    #for casenumber in docs:
    #    f=open('docs/'+casenumber+'.txt')
    #    f.write(docs[casenumber])
    #    f.close()
    #return
    results=[featurize(docs[casenumber],casenumber)for casenumber in docs]
    #print featurize(docs[casenumber],casenumber)
    f.write('\n'.join(results))
    f.close()
    f=open('motions.txt','w')
    f.write('\n'.join(motions))
