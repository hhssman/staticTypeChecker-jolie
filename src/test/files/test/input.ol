// from .importInterface import ImportedInterface
from .embedMe import EmbedMe
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( A )
}

type A: int { x: string }



service MyService() {

	outputPort out1 {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	inputPort in1 {
		location: "local"
		protocol: sodep
		interfaces: MyInterface
	}

	main {
		a = 10
		[helloReqRes(i)(o){
			nullProcess
		}]{
			[helloReqRes(y)(l){
				nullProcess
			}]
		}
	}
}


