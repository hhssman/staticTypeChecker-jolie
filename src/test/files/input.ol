// from .types import C, ImportedInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( A )( string )
	OneWay:
		helloOneway( B )
}


type A: int {
	x: A
	y: string
}

type B: int {
	x: C
}

type C: bool {
	x: double
}

service MyService() {
	inputPort in {
		Location: "socket://localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface 
	}
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		a = 2
		if(4 == 2){
			a = "hey"
		}
		else if(false){
			a = true
		}
		else if(a instanceof int){
			a = 20
		}
		else{
			a = 1L
		}
		// [helloOneway(input)]

		// [helloReqRes(input)(output)]
	}
}