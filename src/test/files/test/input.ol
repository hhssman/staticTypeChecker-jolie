from .importInterface import ImportedInterface
// from .embedMe import EmbedMe
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( rec )
}

type A: bool

type input: void {
	x: rec
	y: rec
}

type rec: int {
	x: string {
		y: rec
	}
}

type huh: int | string

service MyService() {
	inputPort in {
		Location: "socket://localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface 
	}
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: ImportedInterface
	}

	main {
		importedOneWay@out(1)
	}
	
}


