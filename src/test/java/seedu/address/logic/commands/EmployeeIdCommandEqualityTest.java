package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import seedu.address.model.employee.EmployeeId;

public class EmployeeIdCommandEqualityTest {
    private static final List<Function<EmployeeId, Command>> COMMAND_FACTORIES = List.of(
            DeleteCommand::new, ArchiveCommand::new, UnarchiveCommand::new);

    @Test
    public void hashCode_equivalentEmployeeIds_supportsHashCollections() {
        for (Function<EmployeeId, Command> factory : COMMAND_FACTORIES) {
            Command first = factory.apply(new EmployeeId("E0001"));
            Command equivalent = factory.apply(new EmployeeId("e0001"));
            assertEquals(first, equivalent);
            assertEquals(first.hashCode(), equivalent.hashCode());

            Set<Command> commands = new HashSet<>();
            commands.add(first);
            assertTrue(commands.contains(equivalent));
            commands.add(equivalent);
            assertEquals(1, commands.size());

            Map<Command, String> results = new HashMap<>();
            results.put(first, "first result");
            assertEquals("first result", results.get(equivalent));
            results.put(equivalent, "replacement result");
            assertEquals(1, results.size());
            assertEquals("replacement result", results.get(first));
        }
    }

    @Test
    public void equals_differentIdsAndCommandTypes_remainDistinct() {
        Set<Command> commands = new HashSet<>();
        for (Function<EmployeeId, Command> factory : COMMAND_FACTORIES) {
            commands.add(factory.apply(new EmployeeId("E0001")));
            commands.add(factory.apply(new EmployeeId("E0002")));
        }
        assertEquals(6, commands.size());
    }
}
