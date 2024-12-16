//package Main;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//public void writeObjectToFile(String filePath, Object obj) {
//    try (FileOutputStream fos = new FileOutputStream(filePath, true);
//         AppendableObjectOutputStream oos = new AppendableObjectOutputStream(fos)) {
//        oos.writeObject(obj);
//        oos.flush();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//}
//
