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

Currently bad indents, i.e. lines that aren't a multiple of the indent size, are ignored.
However in some mad languages, such as clojure, some lines are indented to "look good" so you get
a lot of lines with a strange indent, such as 
`(#{:foo
    :bar
    :baz})`
    
Here, maybe the odd-numbered lines such as :bar and :baz should count as the nearest even indent?
Possibly not for the initial guess-the-indent game, but maybe when finding indent patterns, and building the
indent array for gui visualization!

