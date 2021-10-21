# SADA Loomings Challenge
Take home interview question for SADA Cloud Technology Services

##Set Up
- Make sure you have a newer version of java, I used 16
- Clone the repo
- Run (or click the file to open) './run.bat' if on Windows or './run.sh' if on Linux
- Please let me know if the Linux script doesn't run properly or displays with terrible formatting. I had some issues getting it to work on my own computer so the script is... interesting right now

##Implementation Details
I started by following the linked refernce to creating a bucket and uploading a file to it. I then adjusted that code to fit my own definitions of good programming. For example, I used variables for the file names so that it was easier to test different files and so in the future adjusting the code to allow users to input their file by name is easier. I also did a bit of aesthetic resturcting like empasizing the difference between a success logging message vs actual required output so it is clear to the user what events have occured. 

TODO: Add more detail.

I chose the SHA-256 after research informed me that while is it about 20-30% slower than the other options it is more secure. While Product Managers almost always air on the side of security, many users would rather prioritize speed so I was torn. Ultimately, there are more ways to adjust a loading/busy UI so that the user doesn't become impatient or irritated with the experience than there are ways to get back stolen data so SHA-265 was the winner. 


##Thoughts About the Challenge
I enjoyed Loomings and beleive this is a great difficulty level for a take home interview. The most time consuming portions for me were not in writing the actual program, or even framiliarizing myself with the minio API, but in the inital set up and final packaging. I struggled a bit with the Gradle after deciding to use it about a quarter of the way through my project for the hash.

##Follow Up Notes
Q: Think of how you may deal with many hundreds of thousands of files, each of which may have duplicate lines. How would you remove duplicate lines in each file and do so efficiently and within reasonable amount of time?

A: If the instructions remain the same, I would remove them the same way I have here (and this code will work on triplicates and so forth), but I would run the code in parallel on multiple cores. If I could modify the insturctions, I would name the objects based on their position in the orignal file in relation to the emptry lines, or I would remove empty lines in the clean text file. The procedure owuld then be to read whole file in (O(n)), drop all duplicates (O(n)), then write the clean file back down (O(n)). O(n) + O(n) + O(n) = O(n). In my current solution, Hash Set lookup is amortized O(1), so my whole loop right now is also O(n) but it would be more elegant to simply directly remove the duplicate lines rather than consult a set for every line in the file. 


Q: What kind of strategies can enhance search capability over large amount of textual data?

A: Like in this challenge, you could hash each line, but that only works if you search the exact line. Hash each line (or sentence), make a set of all hashes, hash each search, check if in set. But phrases wouldn't work for here and in most use cases you want a "fuzzy" match, or you're are looking for a phrase (and all it's instances), or a few correct keywords in a phrase. Ultimately, if you want speed, you need to know the purpose of the text so you can select the best way to categorize it in a data structure or graph. 


Q: What kind of cloud services can be used in addition to storing data in an object storage like minio, s3 or gcs? Can you use databases? If so, what are some example databases you can choose to use for this? Can other services be used together? What do you gain by using other services with object storage services?

A:

Sources:
https://docs.min.io/docs/java-client-quickstart-guide.html
https://docs.min.io/docs/java-client-api-reference
https://www.baeldung.com/sha-256-hashing-java
https://www.freecodecamp.org/news/md5-vs-sha-1-vs-sha-2-which-is-the-most-secure-encryption-hash-and-how-to-check-them/
https://docs.oracle.com/javase/7/docs/api/index.html
https://www.geeksforgeeks.org/java/
https://www.cplusplus.com/reference/cstdio/printf/
