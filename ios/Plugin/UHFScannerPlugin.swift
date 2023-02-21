import Foundation

@objc public class UHFScannerPlugin: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
