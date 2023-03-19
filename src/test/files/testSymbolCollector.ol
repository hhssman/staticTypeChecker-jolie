from .types import imp
from. embedMe import EmbedMe

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( Y )
}

type Y: string {
	x: Y
}

type X: int {
	x: X
}

service MyService(param: int) {
	embed EmbedMe as EmbedMe

	inputPort inputPort {
		Location: "socket://localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface 
	}
	
	outputPort outputPort {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}
}