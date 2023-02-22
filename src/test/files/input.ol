// from .types import C, ImportedInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( rec )
}

// type T: void {
// 	a: A
// 	b: B
// }

type A: int { x: int }
type B: int | bool | double
type C: int | bool | double

type rec: int{
	x: rec
}

service MyService(p: rec) {
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
		helloOneway@out(p)
	}
}