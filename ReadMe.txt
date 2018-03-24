BACKUP YOUR WORLDS

Important things first, config and Plugin may not work as intended,
executing the /cc command or using the dedicated version may apply
unwanted irreversible changes (even though the plugin version has an undo function),
so BACKUP YOUR WORLDS AND FILES

That said let's start with this guide to a world of looking forward to command changes in future minecraft versions!

WHAT IS COMMANDCORRECTOR?

CommandCorrector is a tool, which works exactly like the "find > replace" function of numerous editors,
except it has more features, and also works in minecraft (yay)

"ALSO" WORKS IN MINECRAFT?

Yes, dear reader, even though this java application is advertised as minecraft plugin,
it can also be run without any server or minecraft at all! but we'll come back to that later.

GREAT. HOW DOES IT WORK?

Glad you asked, totally not scripted question-asking-question-asker!
After you've put this plugin into the plugins folder of your server,
you can start the server up, and head ingame.
This plugin offers 5 commands:

/commandblockcorrectorconfigreload
/commandblockcorrect
/commandblockcorrectfind
/commandblockcorrecttest
/commandblockcorrectundo

Obviously no one wants to write these long ass names so all of them have abbreviations!

Let's start with commandblockcorrectorconfigreload:
Short: /ccr

WHAT CONFIG?

Well, thanks for interrupting me, but as i forgot what i wanted to say, i'll just answer your question.
There might be cases, where you want to execute the same set of correction commands on multiple places,
let's say you want to update all commandblocks from 1.12 to 1.13 with hundreds of corrections in 3 worlds on 13 creations you've made,
and imagine doing that by updating with one correction at a time... You might do it as well by hand, maybe even faster.
That's why after the first startup, the plugin generates a folder called CommandCorrector with a config in it.
In there you can define as many corrections as you want.
To see how you format the config, look at the default config that is generated,
but please wait with that until you're done with this tutorial.

First, let's figure out the syntax for commandblockcorrect:
Short: /cc

First argument of /cc is the space you want the commandblocks to be corrected.
For that, you can either use a radius, originating from your position,
or if you have worldedit installed on your server,
you may also use "selection", which obviously uses your selected area.

That alone is already a working command!
Once executed, all commands in the defined area will be corrected
with the defined corrections from your config file.

For spontaneous changes, you can also define a correction within the command.
Syntax for that is "/cc radius pattern target assertion".

...WHAT?

Yes, i thought so..
Let's explain this piece by piece.
/cc radius is the same as before.
After that comes pattern and target.
Remember my example of the editors "find > replace"?
It's exactly the same. Pattern is what you want the plugin to find,
Target is what you want it to be replaced with.

Let's do an example for that!
Imagine you have a command that contains "Hi, Pete!" directly infront of you (you can try this ingame if you want)
and you want it to say "Hello, Pete!".
Just tell the command what it has to find > "Hi"
and the replacement > "Hello".

So your full command is "/cc 5 Hi Hello"

BUT THOSE ARE ONLY SINGLE WORDS, HOW DO I CHANGE SENTENCES?

Ah right, sentences. You would think it's just the same procedure as above right?
Wrong!

Minecraft commands get split on every space before they are delivered to me.
That means, i can't tell afterwards where your pattern ends and your target starts.
For that i have defined a "line-break" symbol, that you can use to mark the beginning and ending of sentences: ";/"

With that knowlege let's try a similar example:
this time you have a command that contains "Hi, I'm Pete!"
and you once again want it to change to "Hello, Pete!".

The resulting command will be: "/cc 5 /; Hi, I'm ;/ Hello,"
The last line-break can be left away, as it is redundant.

BUT THERE WAS ANOTHER PARAMETER... ASSERTIONS?

Yes! In some cases, you only want the correction to be happening, when you can be sure, that a certain string is NOT in your command.

BUT WHAT I WANT TO MAKE SURE THAT A STRING IS?

We'll come to that later, for now let's just care for the assertion.

Another example:
this time, you have a lot of commands that either contain "Hi, I'm <MyName>!" or "Hi, I'm not <MyName>!"
but you only want to change the ones that indeed told their name.

In this case: All the commands that do not contain the word "not".
So thats what your command would look like: "/cc 5 /; Hi, I'm ;/ Hello, ;/ not"

NOTE: These explicit parameter-breaks ";/" have to be either used completely or not at all!
"/cc 5 /; Hi, I'm Hello not" has too few arguments as "Hi, I'm Hello not" is read as pattern.
"/cc 5 /; Hi, I'm ;/ Hello not" will replace "Hi, I'm" with "Hello not"
"/cc 5 Hi, I'm ;/ Hello /; not" will replace "Hi," with "I'm", "Hello" is the assertion, and "not" will throw an error because of too many arguments

ALRIGHT. WHAT ABOUT ALL THOSE OTHER COMMANDS?

The next one will be commandblockcorrectfind:
Short: /ccf

The sytax is almost the same as /cc, except as you don't want to change any command but only find it,
you only need /ccf radius pattern.

The plugin will then proceed to look for commands in your specified area
and list them in your chat.

All commandblocks found can be clicked on in chat, which will teleport you to it's location.
Nifty, eh?... No?... Ok let's continue then..

Next is commandblockcorrecttest:
Short: /cct

This one once again works like /cc, but instead of specifying an area to look in,
you give it the input string straight away.
The syntax is "/cct input pattern target assertion"

The reason this command exists is, some pattern are too complicated to tell if they're working as intended right away,
so this is your option to test if it's working!

Example would be: "/cct ;/ Hi, I'm Steve! /; Hi, I'm ;/ Hello, ;/ not"
Which the plugin would answer with "Result would be: Hello, Steve!"

BUT I'M JUST WRITING PLAIN STRINGS, HOW IS THIS TOO COMPLICATED?

Well, not yet, but this plugin offers way more! We'll talk about that later.
But for the sake of this tutorial,
Have a look at this, and tell me where the error is:
/cct [g=3] ;/ ;?([\\[, ]{1});>(g|gamemode)<;;?(?: *)=;?(?: *);>(0|s|survival)|(1|c|creative)|(2|a|adventure)|(3|sp|spectator)<;;?([\\], ]{1}) ;/ ;:(1);:(3)=;:(2);:(4)
Without testing this line ingame, can you tell where the error is?
I thought so. Executing this will instantly tell you whats wrong (you can try that if you want, it's commandblockcorrecttest, what can possibly go wrong)
DISCLAIMER: This error will only be obvious, if you are familliar with commandblocks, but i just assume thats what you're here for.

SO WHAT EXACTLY DOES THIS PATTERN DO?? I ONLY SEE SPECIAL CHARACTERS...

Regex my friend.. Regex. But we'll come to that later ;)

Next command is commandblockcorrectundo:
Short: /ccu

If you noticed, that unwanted corrections were made,
you can undo up to 5 correction commands you executed!

this command also has the parameter "/ccu force" (Which is currently not working, punch me if i forget to remove this once it's working)
which forces commandblocks that have been changed in direction or completely removed,
to be put back in the state where the /cc command was executed.

Now, those were all the commands, any questions?

YOU SAID THIS CAN BE USED WITHOUT MINECRAFT AT ALL. HOW?

Ah, yes, perfect timing! Now that you know how the plugin works,
let's take care of the dedicated version.

UH..

No the dedicated version is not another jar you have to download,
it's all within one file!

As you might have seen next to the config in the generated "CommandCorrector" folder
there is another "Dedicated" folder.
In there, you can put all files you want to be corrected. The files can be in any format that a editor like notepad can read.
Examples are .txt, .yml, .mcfunction, .xml

In this case, the corrections you want to apply can only be defined over the config!
And i might want to add, that undo is also not possible, so COPY the files into the "Dedicated" folder, DON'T MOVE them!
(And replace the previous files, once you have verified that the result is correct)
((or even better use the old file as backup incase you notice theres something wrong later))

To run the corrections over your files, either start the jar with a doubleclick,
or over a command prompt if you want to get some information.
Running the jar over the cmd would look like this: java -jar CommandCorrector.jar

With the second way, you can also add the parameter "append".
By default, the dedicated corrector corrects each line individually,
but there might be some cases where you want multiple lines to be corrected.
For that, you can use "append" which interprets the file as one single, long line,
with the individual lines seperated by your Systems line seperator. In case of Windows it's "\r\n". (Linux is just "\n", and Mac is "\r")

so a file like this:

	First line
	Second line
	Third line
	
will be read in "append" mode as "First line\r\nSecond line\r\nThird line"


ALRIGHT
THIS IS NOT THE AVERAGE QUESTIONEER SPEAKING, IT'S ME!
I WANT TO MAKE SURE NO ONE MISSES THIS PART.

UP UNTIL THIS POINT, YOU CAN USE THIS PLUGIN TO THE SMALLEST EXTEND,
BUT TO CONTINUE IT IS REQUIRED THAT YOU HAVE AT LEAST BASIC UNDERSTANDING OF REGEX (JAVA FLAVORED)
IF YOU DON'T NEED ANY OF THE FOLLOWING FUNCTIONS YOU MAY LEAVE THIS TUTORIAL AT THIS POINT,
THANKS FOR PARTICIPATING AND HAVE FUN!

Now for the nerds out there:
I'm not gonna give you a detailed tutorial about regex, there are lots of good ones out there in the internet.
From now on, i'll just assume that you know regex, if not, come back to this point once you do.
(i heavily recommend regex101.com for getting used to, and later trying out regex patterns before executing them in my plugin)

Oooookay now for the beefy stuff.
The following syntax will be for any "pattern" and "target" no matter if you use it in the dedicated version, in /cc, in /cct or /ccf

Let's start with an easy example,
Your commandblock contains "Hey, I'm Fred, who are you?"
and you want to replace it with "I'm Fred too"

So far you only replaced a hard-coded string with another hard-coded string,
But as you require regex by now, you probably know where this is going.
This time, we want to find a name, but any name. He could be called Olaf, or John, or Dinkelberg.

Alright, as usual, i'll give you the command, and we'll break it down into pieces afterwards.
The solution for this would be pattern = "Hey, I'm ;?(\w+), who are you?", target = "I'm ;:(1) too"

Ok, what do we see here?
We see a \w+ where your regex senses should be tingling (if not, it means the same as [a-zA-Z0-9_]+ )

BUT WHY IS IT ENCLOSED IN ;?() ??

Oh, you're still there... yay..
Ok, imagine the average user, kinda fluent with commandblocks, but never heard of regex.
Say i interpret the whole "pattern" as regex pattern,
the average user will execute something like "/cc 100 ;/ [c=1] ;/ [limit=1]"
(which will replace the "minecraft 1.12" limit abbreviation "c" with it's "minecraft 1.13" counterpart "limit")

my regex interpreter will then see [c=1] as character class and proceed to replace every 'c', '=' and '1' in a radius of 100 with "[limit=1]"
so a command like "[c=1]" will turn into [[limit=1][limit=1][limit=1]]
not really average-user friendly...

thats why i interpret the pattern as string, but as regex is awesome shit i had to think of a way to "mark" parts where regex can stick it's nose in.
i basically had two choices. mark at the beginning of the string, that the whole thing is a regex pattern,
(which puts you in charge for escaping all these special characters like "()[]^$+*?")
but the problem with that is, that i still have all these special functions that you will get to know later,
which need a special wrapping again, so i chose to interpret all as string and mark any part that contains regex.

FOR THE PROS:
I'm not actually interpreting that thing as string, and then go over each defined regex-group individually,
i'll just make sure, everything outside of these regex-passages is properly escaped, which makes it "string-like" for the regex parser.
That means, you can use cool regex stuff like /1 to use the content of the first matched group in another one

Ok, short recap, to define a regex group, use ";?()", and to re-use what was found in this group, use ;:(1) in the target string.

Basically every defined ";?()" in the pattern will reserve a number, if used or not.
That means, this pattern ";?(.) , ;?(.) , ;?(.)" for this input "A , B , C"
will result for a target like this ";:(1) , ;:(2)" in "A , B"

Let's say you don't want the "B", then you have 2 options:
use the target ";:(1) , ;:(3)" for the output "A , C",
or (my preferred choice), make the second a non-capturing group,
so the pattern will be ";:(.) , ;:(?:.) , ;:(.)".
That way, your target will still look like ";:(1) , ;:(2)"
which makes more visible, if you forgot to insert any groups into the target string.

This is a target used in my pre-made config for the update from 1.12 to 1.13:
"@;:(1)[;:(2);:(5);:(7);:(8),;:(3)=;:(6)..;:(4)];:(9)]"
Unfortunately, it's not rare that the amout of groups reaches two digits.
That way, you can just count through to see if all wanted regex groups are used.
If just left away instead of made non-capturing beforehand, this target would contain 3 more groups somewhere inbetween.
Good luck finding an error..

There are more funtions to be seen in terms of patterns,
but before i want to go over 2 features of the target parser.

First one: Notifications!
Have the following problem:
in minecraft 1.12, you could just specify a damage value in f.ex. setblock
like this: "/setblock ~ ~ ~ anvil 6" which places a slighly damaged anvil facing west.
In 1.13, this will no longer be valid, the new syntax is: "/setblock ~ ~ ~ anvil[facing=west,damage=1]"
To update this behavior with this plugin you would need hundreds of corrections, as the damage-value has a different meaning for different blocks.

Now back to the notifications. As it is easier to change these values by hand, it would be nice, if there was a way to notify the user where manual editing is required.
You guessed it, there is!

To notify the user wherever a correction was applied, you can specify the spot in the target string like this:
"Notification right here: ;!(Notification text in here!)"
NOTE: for the notification text you can only use \w ( a-zA-Z0-9_) and " .,!?"

The second one isn't that straight forward, but a lot more useful.
It is like the simple group-insert ";:(1)", but with a lot more functionality.
It's oviously explained best on an example so imagine having this input:
"This is a test" and many more like "This is a fail" and "This is a fake-test"
and a pattern like this: "This is a ;?(.+)"
and for the output we want to categorize them into tests and non-tests.

My solution is this target: "This is ;:1(a ;:(1)):(test):;;:1(no test):!(test):;"
This will output either "This is a test" or "This is no test"

SO MANY ;:;:;;;:

Oh.. I thought you were asleep by now average questioneer..

But yeah, i was about to break it down. what you might have seen is ;:(1) which you already know of.
This will be replaced, before anything else in here will be interpreted, so lets do it in this example as well:
"This is ;:1(a test):(test):;;:1(no test):!(test):;"

Now what do we have here?
;:1 is like ;:(1). I defines that we're talking about the capturing group 1.
After that a string (a test) followed by a ":" and another string (test) and the endDefinition ":;"
Everything after that is another use of this function, so let's care for ";:1(a test):(test):;" for now

What this does is, it checks if the group equals the string in the right brackets, and if it does,
it inserts the string from the left bracketpair into the output string
(else it just will be left out)

You can also check if it's everything but said string with an exclamation mark before the bracket, as in example 2:
";:1(no test):!(test):;"

NOTE: The asserting string (right) will be interpreted as regex pattern, and as all the casuals are left behind by now,
you'll have to escape all these special characters on your own.

So what can you do with it?
- You can attach a string to your group, if it is what you expected.
Example: "Here is a person. ;:1(His name is ;:(1).):(\w+):;"
If there is a name in group 1 (so it's not empty)
the output will be for example: "Here is a person. His name is Steve.".
If group 1 is empty (or doesn't match \w+) the output will be: "Here is a person."

- You can do more or less switch (or if else) statements.
Let's expand the last example:
"Here is a person. ;:1(His name is ;:(1).):(\w+):;;:1(He has no name.):!(\w+):;".
This time, the alternative output will be: "Here is a person. He has no name."

- And maybe more, but thats all i can think of right now ;)

Those were the 2 features of target, let's get back to pattern.
Don't worry though, there is only one more function!

Let's go on a little imagination trip once again.
Imagine, there was a way to check if a player was in the gamemode creative,
something like gamemode=creative.
And imagine, this syntax was the only accepted by minecraft 1.13..
Also imagine 1.12 accepted g=1, g=c, g=creative, gamemode=1, gamemode=c, gamemode=creative.
And now imagine there were 4 gamemodes like this..
That would result in 20 corretions, only for this short line..

Obviously this isn't some voodoo from imaginationland, but a very real issue i'm faced with, when updating to 1.13.

The solution is this:
";>(g|gamemode)<;=;>(0|s|survival)|(1|c|creative)|(2|a|adventure)|(3|sp|spectator)<;"

To understand what it does, let's take the left one: ";>(g|gamemode)<;".
";><;" again defines the start and end of this special function.
For the inside, we have a regex-like syntax.
In that way, it just matches "g" or "gamemode" and puts the result into the capturing group.
In this case, it's almost the same, it matches any of these arguments, but if successful,
puts the last (right) argument into the capturing group.
This is useful, if you have many abbreviations / symbols / synonyms, but you want / need it to be in a single way.
As seen for example "survival" meant in this context the same as "s" or "0", but in 1.13 it is the only accepted keyword.
This sadly doesn't work with the input or pattern containing any regex interpreted special characters ( \/()[]{}?*+.$^| ), i may look into this at some point


But that's it! You can now call yourself an expert in using the CommandCorrector.
Have fun with it ;)
If you experience any bugs, open up an issue on the github page https://github.com/SlaxXxX/CommandCorrect/issues