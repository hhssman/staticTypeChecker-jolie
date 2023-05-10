interface TypeHintInterface{
	OneWay:
		assert(bool)
}

service Typehint() {
	inputPort TypehintInput {
		Location: "local"
		Interfaces: TypeHintInterface
	}
}