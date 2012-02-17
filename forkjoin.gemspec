# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{forkjoin}
  s.version = "1.0.1"
  s.authors = ["Charles Oliver Nutter", "Nakamura Hiroshi"]
  s.date = Time.now.strftime('%Y-%m-%d')
  s.description = "A JRuby extension to efficiently wrap the JSR166 Fork/Join framework."
  s.email = ["headius@headius.com", "nahi@ctor.org"]
  s.files = Dir['lib/**/*'] + Dir['examples/**/*.rb'] + Dir['{README.md,forkjoin.gemspec}']
  s.homepage = "http://github.com/headius/forkjoin.rb"
  s.require_paths = ["lib"]
  s.summary = "A JRuby wrapper around Fork/Join"
  s.test_files = Dir["test/test*.rb"]
  s.platform = "java"
  s.add_dependency("jsr166y")
end
