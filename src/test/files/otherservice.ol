interface EmbedMeInterface1 {
	RequestResponse:
		embedHelloReqRes( string )( string )
	OneWay:
		embedHelloOneWay( string )
}

interface EmbedMeInterface2 {
	RequestResponse:
		embedHelloReqRes( string )( string )
	OneWay:
		embedHelloOneWay( string )
}

service EmbedInService(count: int) {
	execution{ concurrent }

	// inputPort EmbeddedInputPort1 {
	// 	Location: "local"
	// 	Protocol: sodep
	// 	Interfaces: EmbedMeInterface1
	// }

	inputPort EmbeddedInputPort2 {
		Location: "local"
		Protocol: sodep
		Interfaces: EmbedMeInterface2
	}

	main {
		embedHelloReqRes( req )( res ){
			res = "Hello " + req
		}

		embedHelloOneWay( req )
	}
}

service EmbedAsService(count: int) {
	execution{ concurrent }

	inputPort EmbeddedInputPort1 {
		Location: "local"
		Protocol: sodep
		Interfaces: EmbedMeInterface1
	}

	inputPort EmbeddedInputPort2 {
		Location: "local"
		Protocol: sodep
		Interfaces: EmbedMeInterface2
	}

	main {
		embedHelloReqRes( req )( res ){
			res = "Hello " + req
		}

		embedHelloOneWay( req )
	}
}