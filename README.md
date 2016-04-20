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

