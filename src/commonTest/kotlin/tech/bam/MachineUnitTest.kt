package tech.bam

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import tech.bam.domain.exception.AlreadyRegisteredStateId
import tech.bam.domain.exception.NoRegisteredStates
import tech.bam.domain.mock.TrafficLightStateId.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MachineUnitTest {
    @Test
    fun `it registers states`() {
        val machine = createMachine {
            state(RED)
            state(YELLOW)
            state(GREEN)
        }
        assertEquals(
            machine.stateIds,
            listOf(RED, YELLOW, GREEN)
        )
    }

    @Test
    fun `it rejects already registered states`() {
        assertFailsWith<AlreadyRegisteredStateId> {
            createMachine {
                state(RED)
                state(YELLOW)
                state(RED)
            }
        }
    }

    @Test
    fun `it registers an initial state`() {
        val machine = createMachine {
            initial(RED)
            state(RED)
            state(YELLOW)
            state(GREEN)
        }

        assertEquals(RED, machine.initial)
    }

    @Test
    fun `it uses the first state as initial state when no initial state is declared`() {
        val machine = createMachine {
            state(GREEN)
            state(RED)
            state(YELLOW)
        }

        assertEquals(GREEN, machine.initial)
    }

    @Test
    fun `it fails when no state is passed`() {
        assertFailsWith<NoRegisteredStates> { createMachine {} }
    }

    @Test
    fun `it registers listeners`() {
        val machine = createMachine {
            initial(RED)
            state(RED)
            state(YELLOW)
            state(GREEN)
        }

        val listener = MachineTransitionListener { _, _ ->
            print("I'm listening.")
        }

        machine.subscribe(listener)

        assertEquals(listOf(listener), machine.listeners)
    }

    @Test
    fun `it unsubscribes its listeners`() {
        val machine = createMachine {
            initial(RED)
            state(RED)
            state(YELLOW)
            state(GREEN)
        }

        val listener = MachineTransitionListener { _, _ ->
            print("I'm listening.")
        }

        machine.subscribe(listener)
        machine.unsubscribe(listener)

        assertEquals(listOf(), machine.listeners)
    }

    @Test
    fun `it creates a listener`() {
        val effect =
            mockk<(previousActiveStateIds: List<StateId>, nextActiveStateIds: List<StateId>) -> Unit>()
        every { effect(any(), any()) } returns Unit

        val machine = createMachine {
            initial(RED)
            state(RED)
            state(YELLOW)
            state(GREEN)
        }

        machine.onTransition { prev, next ->
            effect(prev, next)
        }

        machine.listeners[0].callback(listOf(), listOf())

        verify { effect(listOf(), listOf()) }
        confirmVerified(effect)
    }
}