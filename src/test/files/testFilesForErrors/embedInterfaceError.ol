interface EmbedInt1 {
	RequestResponse:
	OneWay:
		myOneWay(void)
}

interface EmbedInt2 {
	RequestResponse:
		myReqRes(void)(void)
	OneWay:
		myOneWay(int)
}

service EmbedMe {
	inputPort embedPort1 {
		location: "local"
		protocol: sodep
		interfaces: EmbedInt1
	}

	inputPort embedPort2 {
		location: "local"
		protocol: sodep
		interfaces: EmbedInt2
	}

	main{
		nullProcess
	}
}

interface MainInt {
	RequestResponse:
		myReqRes(void)(void)
	OneWay:
		myOneWay(void)
}

service Main {
	outputPort mainPort {
		interfaces: MainInt
	}

	embed EmbedMe in mainPort

	main{
		nullProcess
	}
}