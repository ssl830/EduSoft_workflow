import re
import matplotlib.pyplot as plt
import sys
import os
import numpy as np

import matplotlib
matplotlib.rcParams['font.sans-serif'] = ['SimHei']  # 支持中文
matplotlib.rcParams['axes.unicode_minus'] = False

# 文件名处理同前
if len(sys.argv) > 1:
    filename = sys.argv[1]
else:
    files = [f for f in os.listdir('.') if f.startswith('curl_benchmark_') and f.endswith('.txt')]
    if not files:
        print('未找到curl_benchmark_*.txt文件')
        sys.exit(1)
    filename = sorted(files)[-1]

with open(filename, encoding='utf-8') as f:
    lines = f.readlines()

start, end = None, None
for i, line in enumerate(lines):
    if '总览表' in line:
        start = i
    if start is not None and line.strip() == '' and i > start:
        end = i
        break
if start is None:
    print('未找到总览表')
    sys.exit(1)

table_lines = lines[start+1:end]

names, avg, min_, max_ = [], [], [], []
for line in table_lines:
    m = re.match(r'\|?\s*([^|]+)\s*\|\s*([\d.]+)\s*\|\s*([\d.]+)\s*\|\s*([\d.]+)\s*\|', line)
    if m:
        names.append(m.group(1).strip())
        avg.append(float(m.group(2)))
        min_.append(float(m.group(3)))
        max_.append(float(m.group(4)))

if not names:
    print('未能解析到数据')
    sys.exit(1)

x = np.arange(len(names))
width = 0.25

plt.figure(figsize=(12, 6))
b1 = plt.bar(x - width, min_, width, label='最小')
b2 = plt.bar(x, avg, width, label='平均')
b3 = plt.bar(x + width, max_, width, label='最大')

for bars in [b1, b2, b3]:
    for bar in bars:
        y = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2, y, f'{y:.2f}', ha='center', va='bottom', fontsize=9)

plt.ylabel('响应时间 (秒)')
plt.title('AI接口响应时间对比')
plt.xticks(x, names, rotation=30)
plt.legend()
plt.tight_layout()

out_png = filename.replace('.txt', '_groupbar.png')
plt.savefig(out_png, dpi=150)
print(f'已生成: {out_png}')