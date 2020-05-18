#!/usr/bin/env python
# coding: utf-8

# In[30]:


import numpy as np
from scipy import spatial
import matplotlib.pyplot as plt
from sklearn.manifold import TSNE
import nltk
from nltk.corpus import wordnet as wn
from functools import reduce
from collections import OrderedDict
from itertools import permutations, combinations, chain
import time


# In[31]:


nltk.download('wordnet')


# In[32]:


def load_glove(path):
    embeddings_dict = {}
    # with open("C:/Users/Lola/Documents/UOC/papers/2020/ASE/vectors_flights.txt", 'r', encoding='utf-8') as f:
    # with open("C:/Users/Lola/Documents/UOC/papers/2020/ASE/glove.6B.300d.txt", 'r', encoding='utf-8') as f:
    with open(path, 'r', encoding='utf-8') as f:
        for line in f:
            values = line.split()
            token = values[0]
            vector = np.asarray(values[1:], "float32")
            embeddings_dict[token] = vector
    print("GloVe Embeddings loaded")
    return embeddings_dict


# In[33]:


def euclidean_distance(glove_emb_dict, w1, w2):
    emb1 = glove_emb_dict[w1]
    emb2 = glove_emb_dict[w2]
    return spatial.distance.euclidean(emb1, emb2)

def find_closest_embeddings(glove_emb_dict, embedding):
    return sorted(glove_emb_dict.keys(), key=lambda token: spatial.distance.euclidean(glove_emb_dict[token], embedding))    

def find_closest_concepts(glove_emb_dict, concepts, negative, num):
    embeddings = glove_emb_dict[concepts[0]]
    for t in concepts: #range(1, len(concepts)):
        embeddings = embeddings + glove_emb_dict[t]
    for t in negative:
        embeddings = embeddings - glove_emb_dict[t]
    closest_terms = find_closest_embeddings(glove_emb_dict, embeddings)[:num] # return the 20 most similar concepts
    # closest_terms = set(closest_terms) - set(concepts) # remove words that we already have
    # print(closest_terms)
    return closest_terms

_empty_powerset = ((), )

def powerset(someset):
    size = len(someset)
    combs = (combinations(someset, k) for k in range(1, size+1))
    return chain(_empty_powerset, *combs)

def get_glove_recommendations(glove_emb_dict, words, negative, num):
    """
    Given a list of words, we don't only compute the closest words to the whole set, but also to its subsets,
    i.e., we have to campute the powerset of the original set.
    For instance, if we have {'1', '2', '3'}, we compute the closest_concepts for:
    {1}, {2}, {3}, {1, 2}, {1, 3}, {2, 3} and {1, 2, 3} and return a set with all the results.
    As we said in the comment of "find_closest_concepts", we should consider these sets as lists, permute them all
    and compute the closest terms for each permutation. Nevertheless, I've observed that the results are very similar
    and the performance degrades a lot, so I've commented the version below (which computes the powerset and then the 
    permutation and I've left a simplified one that only takes into account the powerset (done by hand).
    
    recomm = []
    for subset in tuple(powerset(words)):
        if (len(subset) != 0):
            perm = permutations(subset)
            for subset1 in list(perm):
                concepts = find_closest_concepts(subset1, num)
                recomm = recomm + concepts
    return list(OrderedDict.fromkeys(recomm)) #remove duplicates and return
    """
    aux = []
    recomm = []
    for w in words:
        aux.append(w)
        # if len(negative) == 0:
        concepts = find_closest_concepts(glove_emb_dict, aux, negative, num)
        recomm = recomm + concepts
        # else:
        #    concepts = find_closest_concepts_negative(aux, negative, num)
        #    recomm = recomm + concepts
    return recomm


# In[34]:


def generate_synonym(input_word):
    results = []
    results.append(input_word)
    synset = wn.synsets(input_word)
    for i in synset:
        index = 0
        print(i.name())
        word = 'dog.n.01'
        s = word.split('.')
        print(s[0])
        syn = i.name().split('.')
        if syn[index]!= input_word:
            name = syn[0]
            results.append(PataLib().strip_underscore(name))
        else:
            index = index + 1
            results = {'input' : input_word, 'results' : results, 'category' : 'synonym'} 
            return results 
        
# def is_verb(input_word):
#     synset = wn.synsets(input_word)
#     is_verb = False
#     index = 0
#     while not is_verb and index <= len(synset):
#         syn = synset[index]
#         syn = synset[index].name().split('.')
#         name = syn[0]
#         pos = syn[1]
#         if pos == 'v':
#             is_verb = True
#         index = index + 1
#     return is_verb

def get_pos(input_word):
    # v = verb; n = noun; a = s = adjective; s = ; r = adverb; 
    synset = wn.synsets(input_word)
    is_verb = False
    pos = set()
    for syn in synset:
        print(syn)
        s = syn.name().split('.')
        pos.add(s[1])
    return pos

def exists_as_is(input_word):
    synsets = wn.synsets(input_word)
    exists = False
    i = 0
    while not exists and i < len(synsets):
        syn = synsets[i]
        s = syn.name().split('.')
        if (s[0] == input_word):
            exists = True
        i = i+1
    return exists

def is_verb(input_word):
    pos = get_pos(input_word)
    return 'v' in pos

def is_noun(input_word):
    pos = get_pos(input_word)
    return 'v' in pos

def lemmatize(input_word):
    m = wn.morphy(input_word)
    return m
    
def lemmatize_as_verb(input_word):
    m = wn.morphy(input_word, wn.VERB)
    return m

def lemmatize_as_noun(input_word):
    m = wn.morphy(input_word, wn.NOUN)
    return m
    
# If the word we provide doesn't exists as is in wordnet, we lemmatize it and analyse the result as if it was the word we received.
# If the lemmatization returns None, it is not a proper dictionary word.
#
# Special cases: for instance, running matches with wordnet because it is a noun, but it could also also be the verb 'run'
# after lemmatization)... Right now, I'm not doing anything if it matches. I could return the match plus the result of the
# lemmatization
#
# Note that the lemmatization of running w/o specifying the kind of word (i.e., wn.morphy('running')) would give us the word
# 'running'. If we specify the kind of work, for instance, verb (i.e., wn.morphy(input_word, wn.VERB)) then, it returns 'run'
def process_with_wordnet(input_word):
    # we don't specify the type of lemmatization we want
    if not exists_as_is(input_word):
        word = lemmatize(input_word)
        return word # it will return None if after lemmatization the word doesnt exist
    else:
        return input_word
    
def process_with_wordnet_multiple_lemmatizing(input_word):
    # we lemmatize each word as noun and verb
    result = []
    word = lemmatize_as_verb(input_word)
    if word != None:
        result.append(word)
    word = lemmatize_as_noun(input_word)
    if word != None:
        result.append(word)
    return result # it will return None if after lemmatization the word doesnt exist

def exists_in_wordnet(word):
    return len(wn.synsets(word)) > 0


# In[35]:


def get_suggestions(glove_emb_dict, concepts, negative, num):
    concepts = lower(concepts)
    glove_suggestions = get_glove_recommendations(glove_emb_dict, concepts, negative, num)
    # print('\n*** GloVe suggestions (including subsets) ***')
    # print(glove_suggestions)

    wordnet_words = []
    for w in glove_suggestions:
        if exists_in_wordnet(w):
            wordnet_words.append(w)
        wordnet_words = wordnet_words + process_with_wordnet_multiple_lemmatizing(w)
    wordnet_words = list(OrderedDict.fromkeys(wordnet_words))

    wordnet_words = [ele for ele in wordnet_words if ele not in concepts] # remove words that are already present
    wordnet_words = [ele for ele in wordnet_words if ele not in negative] # remove words that are negative words
    # print('\n*** Suggestions (removing existing words in the model) ***')
    return wordnet_words

def lower(x):
    return [element.lower() for element in x] ; x


# In[23]:


# context_embeddings_dict = load_glove("C:/Users/Lola/Dropbox/UOC/papers/2020/ASE/vectors_emasa_en.txt")
context_embeddings_dict = load_glove("C:/Users/Lola/Dropbox/UOC/papers/2020/ASE/eAdministration_cs/vectors_eadministration.txt")


# In[19]:


start = time.time()
general_embeddings_dict = load_glove("C:/Users/Lola/Documents/UOC/papers/2020/ASE/glove.6B.300d.txt")
end = time.time()
print(end - start)


# In[26]:


def suggest(concepts, negative, num):
    words = get_suggestions(context_embeddings_dict, concepts, negative, num)
    print("** Contextual knowledge: \t %s" % (words), sep='')
    words = get_suggestions(general_embeddings_dict, concepts, negative, num)
    print("** General knowledge: \t %s" % (words), sep='')
    print()


# In[43]:


suggest(["lab"], [], 10)
    # From this query, we take the following words
    #   Contextual: supervisor, status, order, duplicate
    #   General: -


# In[12]:


start = time.time()
# euclidean_distance(general_embeddings_dict, 'plane', 'airport')
get_suggestions(general_embeddings_dict, ['hello'], [], 5)
end = time.time()
print(end - start)


# In[22]:


print(euclidean_distance(general_embeddings_dict, 'order', 'change'))
print(euclidean_distance(context_embeddings_dict, 'order', 'change'))


# In[ ]:




