package fivebuttons

import kotlin.system.exitProcess

/** The color the lights can be. */
enum class Color(val printableName: String) {
  OFF("X") {
    override fun next() = RED
  },
  RED("R") {
    override fun next() = GREEN
  },
  GREEN("G") {
    override fun next() = OFF
  };

  abstract fun next(): Color
}

/** Represents the state of the fivebutton tricolor problem. */
data class State(val lights: List<Color>) {
  companion object {
    val ALL_OFF = State(listOf(Color.OFF))
  }

  /** How many buttons exist. */
  var numButtons = lights.size

  /** The range of buttons. */
  var buttons = 0..numButtons - 1

  /**
   * Returns a new state after pushing button n.
   *
   * Button i increments the color of the corresponding light and the lights above and below it
   * (wrapping).
   *
   * @throws IllegalArgumentException when an invalid button is provided.
   */
  fun push(button: Int): State {
    if (!buttons.contains(button)) {
      throw IllegalArgumentException("Button out of range ${buttons}")
    }
    val newLights = lights.toMutableList()
    if (button == 0) {
      newLights[buttons.endInclusive] = newLights[buttons.endInclusive].next()
      newLights[0] = newLights[0].next()
      newLights[1] = newLights[1].next()
    } else if (button == buttons.endInclusive) {
      newLights[button - 1] = newLights[button - 1].next()
      newLights[button] = newLights[button].next()
      newLights[0] = newLights[0].next()
    } else {
      newLights[button - 1] = newLights[button - 1].next()
      newLights[button] = newLights[button].next()
      newLights[button + 1] = newLights[button + 1].next()
    }
    return State(newLights.toList())
  }

  /** Returns a visual representation of the state, like "GGG_R". */
  fun printableState(): String {
    return lights.map { it.printableName }.joinToString("")
  }
}

/** Returns a State from the state representation. */
fun stateFromString(stateString: String): State {
  if (stateString.length < 3) {
    throw IllegalArgumentException("State must be at least 3 characters, got ${stateString.length}")
  }
  val lights = mutableListOf<Color>()
  for (s in stateString.asSequence()) {
    when (s) {
      // Gg
      'G',
      'g' -> lights.add(Color.GREEN)
      // Rr
      'R',
      'r' -> lights.add(Color.RED)
      // _ Xx
      '_',
      ' ',
      'X',
      'x' -> lights.add(Color.OFF)
      else ->
        throw IllegalArgumentException(
          "Invalid light color: $s; must be green (Gg), red (Rr), or off (_Xx )"
        )
    }
  }
  return State(lights.toList())
}

/**
 * Where we are and how we got here.
 *
 * @attribute state current state.
 * @attribute pushed the last button pushed to get to this state. -1 if no button has been pushed.
 * @attribute prev the previous step in the Path (if any).
 */
data class Path(val state: State, val pushed: Int, val prev: Path?) {

  companion object {
    val NO_BUTTON = -1
  }

  /** Returns the next steps along the path that lead to un-seen states. */
  fun next(seen: MutableSet<State>): List<Path> {
    val newPaths = mutableListOf<Path>()
    for (i in state.buttons) {
      val newState = state.push(i)
      if (seen.contains(newState)) {
        continue
      }
      seen.add(newState)
      newPaths.add(Path(state = newState, pushed = i, prev = this))
    }
    return newPaths.toList()
  }

  /** Returns a printable version of the button name (no button = blank). */
  fun printableButton(): String {
    if (pushed == NO_BUTTON) return " "
    return pushed.toString()
  }

  /** Returns a printable version of all the steps along path. */
  fun printablePath(): List<String> {
    val steps = mutableListOf<String>()
    var step: Path? = this
    while (step != null) {
      steps.add("${step.printableButton()}: ${step.state.printableState()}")
      step = step.prev
    }
    return steps.reversed().toList()
  }
}

class NoPathExists(message: String) : Exception(message)

class OutOfStates(message: String) : Exception(message)

/**
 * Returns the shortest path from start to goal. Only maxStates will be considered.
 *
 * @throws NoPathExists when there is no path from start to goal.
 * @throws OutOfStates when more than maxStates are required to find whether or not a path exists.
 */
fun search(start: State, goal: State, maxStates: Int): Path {
  val root = Path(state = start, pushed = Path.NO_BUTTON, prev = null)
  if (goal == start) return root
  val seen = mutableSetOf<State>()
  seen.add(start)
  val nextSteps = ArrayDeque<Path>(50)
  nextSteps.add(root)
  var stepsTaken = 0
  while (nextSteps.isNotEmpty() && stepsTaken < maxStates) {
    val next = nextSteps.removeFirst()
    for (step in next.next(seen)) {
      stepsTaken++
      if (step.state.equals(goal)) {
        return step
      }
      nextSteps.addLast(step)
    }
  }
  if (nextSteps.isEmpty()) {
    throw NoPathExists("Can't reach ${goal.printableState()} from ${start.printableState()}")
  }
  throw OutOfStates(
    "Can't reach ${goal.printableState()} from ${start.printableState()} within ${maxStates} iterations"
  )
}

// 10_000 would be a few seconds of processing time. You can exhaust 5 buttons in 80 states.
val MAX_ITERATIONS = 10_000

data class Problem(var start: State, var goal: State) {
  companion object {

    /**
     * Returns a parsed Problem.
     *
     * @throws IllegalArgumentException if either start or goal is invalid.
     */
    fun parse(start: String, goal: String): Problem {
      val problem = Problem(start = stateFromString(start), goal = stateFromString(goal))
      if (problem.start.lights.size != problem.goal.lights.size) {
        throw IllegalArgumentException("Start and Goal must have the same number of lights")
      }
      return problem
    }
  }
}

fun main(argv: Array<String>) {
  if (argv.size != 2) {
    System.err.println("Usage: START GOAL")
    exitProcess(1)
  }
  var problem = Problem.parse(argv[0], argv[1])
  println(
    "Searching path from ${problem.start.printableState()} to ${problem.goal.printableState()}"
  )
  val path = search(problem.start, problem.goal, MAX_ITERATIONS)
  println("Found solution!")
  for (step in path.printablePath()) {
    println(step)
  }
}
