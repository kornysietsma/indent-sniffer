# indent-sniffer

A Clojure library designed to guess complexity stats based on indentation within files

## Usage

`lein run file1.ext file2.ext ...`

`lein run -h` for more help.

If you have a jar, instead of `lein run` use `java -jar indent-sniffer.jar`

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## TODO

### Bad indents

Currently bad indents, i.e. lines that aren't a multiple of the indent size, are ignored.
However in some mad languages, such as clojure, some lines are indented to "look good" so you get
a lot of lines with a strange indent, such as 
`(#{:foo
    :bar
    :baz})`
    
Here, maybe the odd-numbered lines such as :bar and :baz should count as the nearest even indent?
Possibly not for the initial guess-the-indent game, but maybe when finding indent patterns, and building the
indent array for gui visualization!

So: split into "find the best indent" and then "use the best indent".

Indenting with spaces still needs to treat tabs arbitrarily - maybe a tab is 8 characters?
Indenting with tabs can probably ignore spaces?  Or make a space 1/4 of a tab

### Deal with relative filenames and globbing

Maybe rather than globbing we should take a directory and a set of extensions?
the main problem is currently we are dependant on zsh or similar.

### Alternative algorithms

I wonder if there's some way other than assuming even indents to go?  I saw a big body of code that went
`
  4444Foo
  666666bar
  666666bat
`
The pattern of indents won't work nicely - my algorithm will see this as an indent of 2 spaces.
You could work on the assumption that when you go *right* you go by a single indent in almost all languages.
And when you go *left* it's harder to know what it means.

