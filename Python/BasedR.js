
low_pass_input = []
low_pass_output = []


def low_output(n):
    return low_pass_output[n] if n >= 0 else 0


def low_input(n):
    return low_pass_input[n] if n >= 0 else 0


def low_pass_filter(new_input):  # y(n) = 2y(n-1) - y(n-2)+x(n) - 2x(n - 6) + x(n -12)
    low_pass_input.append(new_input)
    if len(low_pass_input) > 13:
        low_pass_input.pop(0)
    current_output_index = len(low_pass_output) - 1
    current_input_index = len(low_pass_input) - 1
    output = 2*low_output(current_output_index-1) - low_output(current_output_index-2) + \
             low_input(current_input_index) - 2*low_input(current_input_index-6) + low_input(current_input_index-12)
    # print 'first', low_pass_input
    # print 'out', output
    low_pass_output.append(output)
    if len(low_pass_output) > 3:
        low_pass_output.pop(0)
    return output

# print raw
lowed = []
for i in raw:
    lowed.append(low_pass_filter(i))
    # break

print raw
print lowed
