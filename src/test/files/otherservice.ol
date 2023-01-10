interface Huh {
	RequestResponse:
		embedHelloReqRes( string )( string )
	OneWay:
		embedHelloOneWay( string )
}

interface MyInterface {
	RequestResponse:
		embedHelloReqRes( string )( string )
	OneWay:
		embedHelloOneWay( string )
}

service EmbedMe {
	execution{ concurrent }

	inputPort EmbeddedInputPort {
		Location: "localhost:8082"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		embedHelloReqRes( req )( res ){
			res = "Hello " + req
		}

		embedHelloOneWay( req )
	}
}