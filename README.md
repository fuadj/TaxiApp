#TaxiApp

A J2ME taxi routing app that applies machine learning to improve route suggestions.
Training is done by extracting features from the possible paths and seeing
which ones the users wants. The training algorithm is the simple linear regression 
with gradient descent. Initial training data is also included, but subsequent 
user data will overwrite it tailor the model to the specific user. The app supports
English and Amharic(አማርኛ) languages.
