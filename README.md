# Forked from [ClockVapor](https://github.com/ClockVapor) 's [markov-telegram-bot](https://github.com/ClockVapor/markov-telegram-bot)
Updated Kotlin version as well as dependencies since they were either deprecated or weren't functional with other libraries.
Added support for Heroku deployment. For the bot configuration you must make a `config.json` with the following syntax:
```json
{
  "telegramBotToken": "BOT_TOKEN",
  "webhookURL": "WEBHOOK_URL",
  "databaseName": "DATABASE_NAME",
  "replyFrecuence": 90,
  "chatFrecuence": 10,
  "ownerChatId": 123456789,
  "ownerId": 123456789,
  "insults": []
}
```
Where `telegramBotToken` is the bot token, `webhookURL` is the URL to your Heroku app (`https://app-name.herokuapp.com`), 
`databaseName` mongoDB database name, `replyFrequence` is a 0 to 100 value which represents the probability
of the bot to reply to a message in which the bot was quoted, `chatFrequence` which is the same as the last value, but when the bot
isn't quoted. When the bot is mentioned via `@` it always replies. `ownerChatId` is the id of your chat with the bot, so it can
send you messages when the bot starts or when an exception occurs, `ownerId` is your UserId in Telegram, you can get those two
with bots like [Get my ID Bot](https://t.me/getmyid_bot?start=botostore). `insults` is an array of strings which whose entries
will be chosen randomly and sent to the chat when using te `/insult` command.

# markov-telegram-bot

`markov-telegram-bot` is a Telegram bot that builds a Markov chain for each user in groups it is added to, and it uses those Markov
chains to generate "new" messages from those users when prompted. The process is similar to your phone keyboard's predictive text
capabilities.

Markov chains are created per user, per group, so you don't have to worry about things you say in one group appearing in generated
messages in another group.

Since this bot stores the contents of every message sent in groups it is added to, it is advised that you create your own
unique Telegram bot and use this library to control it. I do have my own instance of the bot running, but, for privacy's sake, I
don't allow it to join any groups except a few close-knit ones. **Whoever owns some particular instance of this bot will be able
to see every word said in the bot's groups.**

## Sample Usage

### /msg
To generate a message from a user, use the `/msg` command. The command expects a user mention following it to know which user
you want to generate a message from: `/msg @some_user`. This works with a normal `@` mention and also a text mention for users
who don't have a username. Either way, just type the `@` character and select a user from the dropdown that opens.

You can also include an optional "seed" word following the user mention to tell the bot which word to start with when it generates
the message: `/msg @some_user blah`

### /msgall
To generate a message based on the messages from all users, use the `/msgall` command. Just like with `/msg`, you can
include an optional "seed" word following the command.

### /deletemydata
The `/deletemydata` command allows you to delete your own Markov chain data for the current group. Simply send the command and
confirm your choice when the bot asks.

### /deletemessagedata
The `/deletemessagedata` command allows you to delete a specific message from your Markov chain data for the current group. As a
reply to the message you want to remove, send the command and confirm your choice when the bot asks.

### /deleteuserdata
The `/deleteuserdata` command allows group admins to delete Markov chain data for a specific user in the current group. If you are
an admin, simply send the command with a user mention following it, and confirm your choice when the bot asks:
`/deleteuserdata @some_user`. As with the `/msg` command, just type the `@` character and select a user from the dropdown that
opens.

### /stats
The `/stats` command shows a list of each user's top five most distinguishing words - words they use the most, compared
to everyone else in the group.

### /insult
The `/stats` command choses a random insult in the insult array and mentions the user appended to the message. It will require
a user mention.

## Running the Bot

Create a Telegram bot via @BotFather. Take down your bot's access token, and set its privacy mode to disabled so it can
read all messages in its groups. If privacy mode is enabled, the bot won't be able to build Markov chains. Then, using @BotFather's /setcommands command, copy and paste the following text as your input to set your bot's command list.

    msg - Generate message from a user
    msgall - Generate message based on all users in this group
    deletemydata - Delete your Markov chain data in this group
    deletemessagedata - Delete a message from your Markov chain data in this group
    deleteuserdata - (Admin only) Delete a user's Markov chain data in this group
    stats - Display user statistics
    insult - Insults the user mentioned

Next you need to build and deploy the code. This is done via heroku's maven plugin. Just type `mvn clean package heroku:deploy` on the root folder of the project.
You will need to have an enviornment variable called `MONGODB_URI` whose value is the uri of the DB with credentials.

That's it!
