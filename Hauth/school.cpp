#include <iostream>
using namespace std;
// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
int main() {

    int algro;

    cout << "Please select a algraothium you want to test (1-6): " << endl;
    cin >> algro;

    switch (algro) {
        case 1:
            bool isSmaller;
            bool isEqual;
            int one;
            int two;


            if (isSmaller < one) {
                cout << "You're number is less then 8!" << endl;

            } else if (isSmaller > one) {
                cout << "You're number is greater then 8!" << endl;
            }
            cout << "Please select a number 1-10" << endl;
            cin >> one;
            if (one < 1) {
                cout << "Please select a number between 1-10" << endl;
            } else if (one > 10) {
                cout << "Please select a number between 1-10" << endl;
            }
            cout << "This is alrgo 1" << endl;
            break;
        case 2:
            cout << "This is alrgo 2" << endl;
            break;
        case 3:
            cout << "This is alrgo 3" << endl;
            break;
        case 4:
            cout << "This is alrgo 4" << endl;
            break;
        case 5:
            break;
        case 6:
            break;
        default: 1;
    }


    // TIP Press <shortcut actionId="RenameElement"/> when your caret is at the <b>lang</b> variable name to see how CLion can help you rename it.
    auto lang = "C++";
    std::cout << "Hello and welcome to " << lang << "!\n";

    for (int i = 1; i <= 5; i++) {
        // TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        std::cout << "i = " << i << std::endl;
    }

    return 0;
    // TIP See CLion help at <a href="https://www.jetbrains.com/help/clion/">jetbrains.com/help/clion/</a>. Also, you can try interactive lessons for CLion by selecting 'Help | Learn IDE Features' from the main menu.
}
