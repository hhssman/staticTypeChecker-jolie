from .types import imp
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

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

service MyService(input: X) {
	// inputPort in {
	// 	Location: "socket://localhost:8080"
	// 	Protocol: http { format = "json" }
	// 	Interfaces: MyInterface 
	// }
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		a = 10

		if(input){
			b = "hey"
		}
		else{
			b = true
		}

		// helloOneway@out(input)
		// [helloOneway(input)]
		// [helloReqRes(input)(output)]
	}
}