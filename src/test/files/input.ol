// from .types import C, ImportedInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( rec )
}


type A: int { x: int }
type B: int | bool | double
type C: int | bool | double

type rec: int{
	x: rec
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
		// p.x = 10
		// helloOneway@out(p)

		[helloOneway(input)]{
			input = 10
		}

		[helloReqRes(input)(output){
			output += 10
		}]{
			output = 5
			output = 10
		}
	}
}