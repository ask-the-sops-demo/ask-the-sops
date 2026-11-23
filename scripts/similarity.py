#!/usr/bin/env python3
"""similarity.py: how close in meaning are two phrases? Uses a local Ollama model.
Usage: python3 scripts/similarity.py "phrase one" "phrase two" [model]"""
import json, math, sys, urllib.request

model = sys.argv[3] if len(sys.argv) > 3 else "all-minilm"
# Talk to Ollama directly, even if a company proxy is set.
opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))

def embed(text):
    req = urllib.request.Request("http://localhost:11434/api/embed",
                                 data=json.dumps({"model": model, "input": text}).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(opener.open(req))["embeddings"][0]

a, b = embed(sys.argv[1]), embed(sys.argv[2])
cos = sum(x * y for x, y in zip(a, b)) / (math.sqrt(sum(x * x for x in a)) * math.sqrt(sum(y * y for y in b)))
print(f"{cos:.2f}  ({model})  \"{sys.argv[1]}\" vs \"{sys.argv[2]}\"")
