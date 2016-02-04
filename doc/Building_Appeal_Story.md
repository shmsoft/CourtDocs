# NY Appeal Courts - building appeal story

## Inputs
    
1. We have court cases marked up by Greg, these notes should help to understand what we are looking for in 
identifying appeal story.
2. We have possible reasons for appeals (to be listed). In each specific appeal, we need to extract which reasons were used.
3. We have possible judge's reactions (to be listed), which can be negative, positive, and variations thereof
4. We have appeal results.

## Outputs

Based on the inputs, we want to classify every appeal into a group with the 

* Given reasons for appeal
* Given judge's reactions
* Success of failure of appeal

## How to arrive at the outputs

We may use 

* word2vec, 
* paragraphvectors, 
* or any other method to achieve the desired classification

I (Mark) believe that we need much more investigation as to what we want.