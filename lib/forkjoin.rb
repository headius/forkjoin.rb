# TODO: temporary definition

require 'java'
require File.expand_path('../target/forkjoin.jar', File.dirname(__FILE__))
org.jruby.ext.ForkJoin.new.load(JRuby.runtime, false)
