// from .importInterface import ImportedInterface
// from .embedMe import EmbedMe
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( A )
}

type A: bool { x: A }



service MyService() {
	outputPort out1 {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		a = 10
	}
	
}


