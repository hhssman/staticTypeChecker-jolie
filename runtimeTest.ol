type MessageType: void {
	sender: string
	message: string
	time: int
}

interface MessageInterface {
	OneWay:
		sendMessage( MessageType )
}

service MyService() {
	outputPort messageSender {
		Interfaces: MessageInterface
	}

	main {
		msgObj.sender = "John Doe"
		msgObj.message = "Hello World!"

		sendMessage@messageSender(msgObj)
	}
}