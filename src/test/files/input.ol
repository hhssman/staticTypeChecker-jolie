// from .types import imp
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( X )( string )
	OneWay:
		helloOneway( X )
}


// type A: int {
// 	x: imp
// 	y: string
// }

// type B: int {
// 	x: C
// }

// type C: bool {
// 	x: double
// }

// type D: void {
// 	x: int | string
// 	y: bool | double
// 	z: long
// }

// type choice: int | string

type X: int {
	?
}

// type Y: int {
// 	l: string
// }

service MyService() {
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
		// a = -10 + (-20) + "heyh" - true
		a = 10
		// a.x = "hey"
		// a.y = true

		// b = 50L
		// b.y = "hey"
		// b.y.z = 20

		// a << b

		// helloOneway@out(input)

		// [helloOneway(input)]
		// [helloReqRes(input)(output)]
	}
}