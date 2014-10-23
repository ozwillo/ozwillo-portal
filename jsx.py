#! /usr/bin/python3

import os
import re

f = os.popen('find . -name "*.jsx.js"')

for line in f:
  print("Compiling: " + line.strip())
  os.system("jsx " + line.strip() + " > " + re.sub("\.jsx","",line.strip()))
