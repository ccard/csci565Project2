#! /usr/bin/env ruby

def showUsage
puts <<EOF
getStats.rb <outputcsvfile>
EOF
exit
end

def writeFile(line="")
	@file.puts "Method type,run time,unit" if line.empty?
	@file.puts line unless line.empty?
end

def readFile(file)
	input = File.open file,'r'
	input.each do |line|
		if /(^.+<)(LIST|POST|CHOOSE)(>.+)(\d+ | \d+\.\d+)(\s+)(ms .*$)/ =~ line
			writeFile "#{$2},#{$4},#{$6}"
		end
	end
	input.close
end

showUsage unless ARGV.size == 1

@file = File.open ARGV[0],'w'
writeFile
ls = `ls`
ls.each do |line| 
	if /^log\d{5,}\.log/ =~ line
		puts line
		readFile line.chomp
	end
end

@file.close
