# archived!

I'm archiving this for now - I have a simpler version I'll push soon-ish, which doesn't try to do as much.

# indent-sniffer

A Clojure library designed to guess complexity stats based on indentation within files - in a language agnostic way

## Credit for the idea

Note that I thought of this approach in the shower - and then a day later found that Adam Tornhill described a similar
approach, with research backing it up, in ["Your code as a crime scene"](https://pragprog.com/book/atcrime/your-code-as-a-crime-scene) !

Adam even has a python implementation at https://github.com/adamtornhill/indent-complexity-proxy

I wrote my own implementation though, as Adam's version required you to know the indentation level used, whereas I
hoped to be able to guess at it from observing actual indentation in code.  Also I wanted something that could recursively
go through a whole source tree.

## Usage

`lein run src test foo/bar.clj -e ".clj,.js,.py"`

`lein run -h` for more help.

If you have a jar, instead of `lein run` use `java -jar indent-sniffer.jar`

You can build a jar with "lein uberjar" if you have the source code and Leiningen

## Indentation guessing

The current version of indent sniffer tries guessing indentation levels.

It scans each file, and tries it with four scenarios: tab-based indentation, 2-space, 3-space and 4-space.

It then checks:

- if no scenario works for more than 70% of non-blank lines, it gives up
- if one scenario works for more than 70%, choose that one
- if two or more scenarios work, then try eliminating any that match less than 90% of lines
- if this still doesn't give you one match, take the biggest indent level remaining

This is pretty unscientific.  It's hard to guess well, because comments can't currently be distinguished from code,
many people indent inconsistently, and many languages (like clojure!) encourage you to line up code vertically in
strange ways, so while all your main code structures may be at multiples of 3 characters, your 'let' statement might
not.

Obviously this algorithm could do with more thought and tweaking.  But it works in a lot of cases on codebases I've tried

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## TODO

### Allow you to specify explicitly what the indentation is, possibly by extension

### Provide other output formats

Currently this is designed to spit out info as json data for reading with my code-sniff tools - it might be friendly to 
also provide simple csv output.

The JSON includes individual line lengths so I can build pretty images of the 'shape' of the file, so it's rather verbose
- it might be good to make this optional

### Alternative algorithms

Should this at least default to "1 space" if it can't see anything better?


