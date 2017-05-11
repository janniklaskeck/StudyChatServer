# Protocol v1:

## Client Connection to Server:
1. Server/Client Running
2. Client connects to Server
3. Client sends Login Message
4. Server checks if Client Login isn't already used
    - If already used: Close connection
	- If not, create internal User, send User ID to Client
5. Server confirms Login to CLient
6. Client now can join Channels
	
## Client and Channel Interaction:
1. Client sends ChannelJoin to Server
2. Server checks if Client isn't already member of channel
	- If already Member, ignore message
	- If not member, add Client User to channel, remove Client User from other Channels
3. Client now may send messages to Channel
4. Client exits Channel when sending Logout Message


## Message Structure:
```json 
{
	version: VERSION,				Integer
	type: MSG_TYPE, 				String
	content: 
	{
		MSG_CONTENT 					JsonObject
	}
}
```
### Possible MSG_CONTENT
#### Client to Server:
##### User Join Message Content:
```json
{
	userName: USER_NAME			String
}
```
##### Channel Join Message Content:
```json
{
	channelName: CHANNEL_NAME,	String
	userID: USER_ID					String
}
```
##### Channel Send Message Content:
```json
{
	userID: USER_ID,				String
	message: MESSAGE				String
}
```
##### Channel Exit Message Content:
```json
{
	userID: USER_ID					Long
}
```

#### Server to Client:
##### User Join Message Answer Content:
```json
{
	userID: USER_ID					Long
}
```
##### Channel Join Message Answer Content:
```json
{
	channelName: CHANNEL_NAME,	String
	success: SUCCESS				boolean
}
```
##### Channel Send Message Answer Content:
```json
{
	channelName: CHANNEL_NAME,	String
	success: SUCCESS				boolean
}
```
##### Channel Exit Message Answer Content:
```json
{
	channelName: CHANNEL_NAME,	String
	success: SUCCESS				boolean
}
```