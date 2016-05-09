# PersonalityRecognition

## Notes for write-up
-A Tokenizer trained on OpenNLP's en-token.bin recognizes clitics and heart emojis as tokens, among other things, whereas the simple tokenizer does not.

-Reasoning for not using a stopword filter: On Stopwords, Filtering and Data Sparsity for Sentiment Analysis of Twitter
Hassan Saif,1 Miriam Fernandez,1 Yulan He,2 Harith Alani1
1Knowledge Media Institute, The Open University, UK
{h.saif, m.fernandez, h.alani}@open.ac.uk
2School of Engineering and Applied Science, Aston University, UK

-Wang does not discuss how he chose stopwords in his paper. We assume that he used the common method of a precompiled list. We use a precompiled list as well.