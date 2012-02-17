require 'jruby'
require 'jsr166y.jar'
require 'forkjoin.jar'
org.jruby.ext.ForkJoin.new.load(JRuby.runtime, false)
