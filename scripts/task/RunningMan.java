package task;

import utils.TaskQueue;

//        // add some starter tasks
//        q.add(() -> new Task(TaskType.MINE, "Iron Rock").run());
//        q.add(() -> new Task(TaskType.WALK, "Bank").run());
//        q.add(() -> new Task(TaskType.FISH, "Shrimp Spot").run());
//
//        // urgent event triggers
//        //q.addUrgent(new Task(TaskType.WALK, "Safe Zone"));
//
//public class BotRunner {
//    public static void main(String[] args) {
//        TaskQueue queue = new TaskQueue();
//        // run until we run out of tasks
//        while (queue.hasTasks()) {
//            Task task = queue.next();
//            System.out.println("Running: " + task);
//            boolean success = task.run();
//
//            if (!success) {
//                // retry failed task
//                System.out.println("Retrying: " + task);
//                queue.add(task);
//            }
//        }
//    }
//}