interface ImportedInterface{
	RequestResponse:
		importedReqRes( A )( B )
	OneWay:
		importedOneWay( A )
}

type A: string {
	x: int
}

type B: int {
	x: B
}