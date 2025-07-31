import argparse
import os
import numpy as np
from get_wave import get_app
from libs import audio, imu
import librosa
import threading
from tool import evaluation

isrunning = True
save_fn = None


def stop():
    global isrunning
    if isrunning==False:
        isrunning = True
    else:
        isrunning = False
    print("Record Status"+isrunning)

# 确保数组长度足够，且返回 numpy 数组
def pad_or_repeat_to_length(arr, target_length):
    """如果数组长度不足 target_length，则填充 0 或重复补足"""
    arr = np.array(arr)  # 确保 arr 是 numpy 数组
    if len(arr) >= target_length:
        return arr[:target_length]  # 截断
    else:
        repeat_times = (target_length // len(arr)) + 1
        arr = np.tile(arr, repeat_times)[:target_length]  # 重复填充
        return arr

def create_audio_directory():
    user_name = 'test_audio_only'
    activity = 'act_test'
    audio_dir = os.path.join('data', user_name, activity, 'audio')

    # 确保目录存在
    try:
        if not os.path.exists(audio_dir):
            os.makedirs(audio_dir)
            print(f"目录创建成功: {audio_dir}")
        else:
            print(f"目录已存在: {audio_dir}")
    except Exception as e:
        print(f"创建目录时出错: {e}")

def init(args):

    print("Audio IP: {}".format(args.Audio_IP))
    print('record time(s): {}'.format(args.record_time))

    # 初始化音频对象
    audio_obj = audio.Audio(args.Audio_IP, args.audio_save_name, args.record_time)
    print("init audio ok")
    audio_obj.thread_handler.start() # 先启动线程
    # 检查线程状态
    if audio_obj.thread_handler.is_alive():
        print("线程已成功启动")
    else:
        print("线程未能启动")
    return audio_obj

def get_waveforms(audio_obj):

    global isrunning

    if isrunning==False:
        return

    app_instance =get_app()
    print(save_fn)

    #---
    print("Checking file path:", audio_obj.save_fn)
    print("File exists?", os.path.exists(audio_obj.save_fn))

    audio_data, audio_sr = librosa.load(audio_obj.save_fn, sr=10000, mono=True)
    audio_data = audio_data[:50000]
    # audio_data = audio_obj.get_audio_data(audio_obj.save_fn)
    print("得到波形.....")
    audio_data = np.array(audio_data)
    print(audio_data.shape)

    # 获取 ECG 和 呼吸波形
    predict_ecg_waveform = app_instance.get_predict_ecg_waveform(audio_data)
    predict_breathing_waveform = app_instance.get_predict_breathing_waveform(audio_data)

    print("predict_ecg_waveform length:", len(predict_ecg_waveform))  # 先检查长度
    print("predict_breathing_waveform length:", len(predict_breathing_waveform))

    ecg_waveform = []
    breathing_waveform = []

    ecg_waveform.extend(predict_ecg_waveform)
    breathing_waveform.extend(predict_breathing_waveform)

    ecg_waveform_array = pad_or_repeat_to_length(ecg_waveform, 10000)
    breathing_waveform_array = pad_or_repeat_to_length(breathing_waveform, 5000)

    # 计算 HR 和 BR
    hr_list = evaluation.hr_compute_V2_1(ecg_waveform_array, 1000)
    br_list = evaluation.br_compute_V2(breathing_waveform_array, 250)

    # 再次填充，保证输出一致
    hr_list = pad_or_repeat_to_length(hr_list, 5000)
    br_list = pad_or_repeat_to_length(br_list, 2500)

    print("hr_list shape:", len(hr_list), "values:", hr_list[:10])  # 仅显示前 10 个
    print("br_list shape:", len(br_list), "values:", br_list[:10])  # 仅显示前 10 个

    # 确保返回的是 list
    return list(hr_list), list(br_list)

def run():
    # 创建音频目录
    # create_audio_directory()
    global save_fn
    global isrunning

    if isrunning==False:
        return

    sample = 0
    str0 = "/storage/emulated/0/EPHMonitor/data/test_audio_only/act_test/audio"
    save_fn = os.path.join(str0, '_' + str(sample))
    print(save_fn)
    while os.path.exists(save_fn + '.wav'):
        sample += 1
        save_fn = os.path.join(str0, '_' + str(sample))

    print(save_fn)

    # 参数初始化
    parser = argparse.ArgumentParser()
    parser.add_argument("--Audio_IP", default="192.168.137.67", type=str)
    parser.add_argument("--audio_save_name", default=save_fn, type=str)

    parser.add_argument("--record_time", default=15, type=int)

    # 录制
    args = parser.parse_args()
    audio_obj = init(args)
    return audio_obj

if __name__ == "__main__":
    audio_instance = run()

