#! /usr/bin/env ruby

def showUsage
puts <<EOF
getStats.rb <inputLogfile> <outputcsvfile>
EOF
exit
end

def writeFile(line="")
	@file.puts "Method type,run time,unit" if line.empty?
	@file.puts line unless line.empty?
end

showUsage unless ARGV.size == 2

@file = File.open ARGV[1],'w'
@in = File.open ARGV[0],'r'
writeFile
@in.each do |line|
	if /(^.+<)(LIST|POST|CHOOSE)(>.+)(\d+ | \d+\.\d+)(\s+)(ms$)/ =~ line
		puts line
		writeFile "#{$2},#{$4},#{$6}"
	end
end

@file.close
@in.close
