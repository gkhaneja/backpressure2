comm_tw() {
        [ $# -lt 2 ] && return
        osascript -e "
                tell application \"System Events\" to tell process \"Terminal\" to keystroke \"$1\" using command down
                tell application \"Terminal\" to do script \"$2\" in selected tab of the front window
        " > /dev/null 2>&1
}
newt() {
    comm_tw t "$1"
    comm_tw t "$2"
}
neww() {
    comm_tw n "$1"
}
