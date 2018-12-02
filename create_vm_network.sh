#!/bin/sh

set -e

# get valid emulator ports
get_emulators() {
	adb devices | grep -Po 'emulator-\d+' | cut -d - -f 2
}

# auth token needed for admin operations in emulator console
get_auth_token() {
	cat $HOME/.emulator_console_auth_token
}

# Call telnet commands with sleep for latency
call_fun() {
	sleep 1
	echo "auth $(get_auth_token)"

	echo "ping"
	echo "$1"
	echo "ping"

	sleep 1
	echo "quit"
}

# execute command on the emulator console of the given vm
# 1 - host port
# 2 - command
exec_emulator_console() {
	port="$1"
	call_fun "$2" | telnet 127.0.0.1 "$port" 2>/dev/null | sed -n '/I am alive!/,/I am alive!/p' | sed '/OK/d;1,2d;$d'
}

# get routing redirections for the given vm
# 1 - host port
get_redirections() {
	port="$1"
	exec_emulator_console "$port" "redir list"
}

redir_server() {
	port="$1"
	exec_emulator_console "$port" "redir add $2"
}

undo_redir() {
	port="$1"
	exec_emulator_console "$port" "redir del $2"
}

test_redir() {
	redir_server $1 "tcp:1337:1337"
	get_redirections $1
	undo_redir $1 "tcp:1337"
	get_redirections $1
}

if [ -z "$1" ]; then
	echo "Available emulator ports:"
	get_emulators
	echo "Use args: <server_port>"
	exit
fi

case "$1" in
	test)
		test_redir 5554
		;;
	auto)
		echo "Auto redir 5554"
		echo "Redirecting 1337 for event sender"
		redir_server 5554 "tcp:1337:1337"
		echo "Redirecting 8080 for webrtc"
		redir_server 5554 "tcp:8080:8080"
		redir_server 5554 "udp:8080:8080"
		get_redirections 5554 "tcp:1337:1337"
		;;
	*)
		echo "test - Create and delete redir"
		echo "auto - Create webrtc and event sender redir on first vm"
esac
