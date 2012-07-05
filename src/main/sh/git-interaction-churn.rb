#!/usr/bin/ruby
require 'set'

#input is the revision hash and the file - assume it's correct
revision = ARGV[0]
file = ARGV[1]

#initialize, baby
lines_added = 0
lines_deleted = 0
lines_deleted_self = 0
lines_deleted_other = 0
author = nil
authors_affected = Set.new 

#Use git log to show only that one file at the one revision, no diff context!
patch_text = `git log -p --unified=0 -1 #{revision} -- #{file}`
patch_text.each_line { | line |
	if line.start_with? "Author: " 
		author = line[8..line.index(' <')].chomp # store just the author name
	elsif line.start_with? "@@"
		#parsing the @@ -a,b +c,d @@
		lines_deleted_start = line.split(/[ ]+/)[1].split(/[,]+/)[0] #a 
		lines_deleted_num = line.split(/[ ]+/)[1].split(/[,]+/)[1] # b 
		lines_added_num = line.split(/[ ]+/)[2].split(/[,]+/)[1] #d

		#lines_deleted_start isn't ACTUALLy negative...
		lines_deleted_start = lines_deleted_start.to_i* -1

		# The _num vars are 1 if they were nil, for the ones of this format:
		# @@ -a +c @@ (which implies a 1)
		lines_deleted_num ||= 1  
		lines_added_num ||= 1 
		# ...and they need to be integers
		lines_deleted_num = lines_deleted_num.to_i
		lines_added_num = lines_added_num.to_i

		# Ok, add to the totals now
		lines_added += lines_added_num
		lines_deleted += lines_deleted_num

		#puts "For #{line.chomp}, it's #{lines_deleted_start} #{lines_deleted_num} #{lines_added_num}"
		
		#Now run the blame for the self/other churn
		# * Leading up to the revision prior to that (hence the ^) 
		# * -L for just the lines from the given revision
		# * -l for showing long revision names 
		if lines_deleted_num > 0 then
			blame_text = `git blame -l -L #{lines_deleted_start},+#{lines_deleted_num} #{revision}^ -- #{file}`
			blame_text.each_line { | blame_line | 
				if blame_line.include?(author) #does not contain the author in the line?
					lines_deleted_self+=1
				else
					lines_deleted_other+=1
					author_affected = blame_text.split(/[(]+/)[1].split(/[\d]{4}/)[0].chomp
				    	authors_affected << author_affected	
				end	
			}
		end
	end 
}
puts "Total Churn:\t#{lines_added + lines_deleted}"
puts "Lines Added:\t#{lines_added}"
puts "Lines Deleted:\t#{lines_deleted}"
puts "Lines Deleted, self:\t#{lines_deleted_self}"
puts "Lines Deleted, other:\t#{lines_deleted_other}"
puts "Number of Authors Affected:\t#{authors_affected.size}"
print "Authors Affected:\t"
authors_affected.each{|a| print("#{a}\t")}
puts ""