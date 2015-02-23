#! /usr/bin/python3
# Compile JSX & minify javascript
# Usage of compiled / minified JS is turned on with the "devmode" flag in the Portal
# Note that this requires Node and the following two NPM modules:
# - jsx
# - uglify-js (installed with npm install uglify-js -g to provide the command line wrapper)

import os
import re

f = os.popen('find . -wholename "*/src/*.jsx.js"')

for line in f:
  l = line.strip()
  print("Compiling: " + l)
  os.system("jsx " + l + " | uglifyjs -c drop_console - > " + re.sub("\.jsx",".min",l))
