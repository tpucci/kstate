package com.github.bamlab

class State(
    val value: MachineState,
    val transitions: Map<MachineEvent, Transition>,
    val compoundMachine: Machine?
) {
  var history: State? = null
    set(value) {
      value?.apply { this.history = null }
      field = value
    }
}

class StateBuilder(private val machineState: MachineState) {

  val transitionsMap: MutableMap<MachineEvent, Transition> = mutableMapOf()

  var compoundMachine: Machine? = null

  fun on(event: () -> MachineEvent): TransitionBuilder {
    return TransitionBuilder(this, event())
  }

  fun build(): State {
    return State(machineState, transitionsMap, compoundMachine)
  }

  operator fun Machine.unaryPlus() {
    compoundMachine = this
  }
}