interface EmbedMeInterface {
	RequestResponse:
		embedHelloReqRes( string )( string )
	OneWay:
		embedHelloOneWay( string )
}

service EmbedMe(count: int) {
	execution{ concurrent }

	inputPort EmbeddedInputPort {
		Location: "local"
		Protocol: sodep
		Interfaces: EmbedMeInterface
	}

	main {
		embedHelloReqRes( req )( res ){
			res = "Hello " + req
		}

		embedHelloOneWay( req )
	}
}