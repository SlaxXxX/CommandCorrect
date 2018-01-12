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
that's why after the first startup, the plugin generates a folder called CommandCorrector with a config in it.
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

Next command is commandblockcorrectundo:
Short: /ccu

If you noticed, that unwanted corrections were made,
you can undo up to 5 correction commands you executed!

this command also has the parameter "/ccu force"
which forces commandblocks that have been changed in direction or completely removed,
to be put back in the state where the command was executed.

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
And i might want to add, that undo is also not possible, so copy the files, don't move them!

To run the corrections over your files, either start the jar with a doubleclick,
or over a command prompt if you want to get some information.
Running the jar over the cmd would look like this: java -jar CommandCorrector.jar

With the second way, you can also add the parameter "append".
By default, the dedicated corrector corrects each line individually,
but there might be some cases where you want multiple lines to be corrected.
For that, you can use "append" which interprets the file as one single, long line,
with the individual lines seperated by "\n".