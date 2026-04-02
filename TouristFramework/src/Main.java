import java.lang.reflect.Proxy;
import java.util.Scanner;

import core.Accommodation;
import core.Vehicle;
import proxy.NativeBeanFactory;
import proxy.ProxyHandler;

public class Main {
    public static void main(String[] args) throws Exception {

        NativeBeanFactory<Vehicle> vehicleFactory = new NativeBeanFactory<>("vehicle", Vehicle.class);
        NativeBeanFactory<Accommodation> hotelFactory = new NativeBeanFactory<>("accommodation", Accommodation.class);

        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.println("\n========== 欢迎使用旅游框架 ==========");
            System.out.println("1. 开始新的旅程");
            System.out.println("2. 退出");
            System.out.print("请选择: ");
            String mainChoice = scanner.nextLine().trim();

            if ("2".equals(mainChoice) || "exit".equals(mainChoice)) {
                System.out.println("感谢使用，再见！");
                break;
            }

            if (!"1".equals(mainChoice) && !"travel".equals(mainChoice)) {
                System.out.println("无效选择，请重试");
                continue;
            }

            try {

                System.out.println("\n--- 请选择交通工具 ---");
                System.out.println("可选交通工具: " + vehicleFactory.getNames());
                System.out.print("请选择交通工具: ");
                String vehicleName = scanner.nextLine().trim();

                if (vehicleName.isEmpty()) {
                    System.out.println("交通工具不能为空");
                    continue;
                }

                Vehicle realVehicle = vehicleFactory.createInstance(vehicleName);
                Vehicle vehicleProxy = (Vehicle) Proxy.newProxyInstance(
                        Vehicle.class.getClassLoader(),
                        new Class[]{Vehicle.class},
                        new ProxyHandler(realVehicle)
                );


                System.out.println("\n--- 请选择住宿 ---");
                System.out.println("可选住宿: " + hotelFactory.getNames());
                System.out.print("请选择住宿: ");
                String accommodationName = scanner.nextLine().trim();

                if (accommodationName.isEmpty()) {
                    System.out.println("住宿不能为空");
                    continue;
                }

                Accommodation realAccommodation = hotelFactory.createInstance(accommodationName);
                Accommodation accommodationProxy = (Accommodation) Proxy.newProxyInstance(
                        Accommodation.class.getClassLoader(),
                        new Class[]{Accommodation.class},
                        new ProxyHandler(realAccommodation)
                );


                System.out.println("\n--- 请输入旅行人数 ---");
                System.out.print("请输入人数: ");
                int count = scanner.nextInt();
                scanner.nextLine();

                vehicleProxy.drive();
                accommodationProxy.checkIn(count);

            } catch (Exception e) {
                System.out.println("错误: " + e.getMessage());
//                e.printStackTrace();
            }
        }

        scanner.close();
    }
}
