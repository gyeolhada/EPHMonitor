import argparse
import os
import time


def get_options():
    parser = argparse.ArgumentParser(description='SportEar Implementation')
    # data load setting
    parser.add_argument('--sr', default=[48000, 250, 25, 100, 100, 100], type=int, help='sample rate of audio, ecg, breathing, acc, neck_imu, ear_imu(x_acc, y_acc, z_acc, x_gyro, y_gyro, z_gyro)')
    parser.add_argument('--re_sr', default=[1000, 1000, 1000, 1000, 100, 100], type=int, help='resample rate of audio, ecg, breathing, acc, neck_imu, ear_imu')
    parser.add_argument('--window_len', default=4, type=int, help='the len of the window that segment the data')
    parser.add_argument('--window_slide', default=2, type=float, help='the slide of the window that segment the data')
    # parser.add_argument('--data_dir', default="/data/zyz/data/zephyr data and record data", type=str)
    parser.add_argument('--data_dir', default="/sdf1/zyz/datas/zephyr data and record data", type=str)

    # parser.add_argument('--users_list', default=['user_1'])
    # parser.add_argument('--activities_list', default=['biking'])
    # parser.add_argument('--samples_list', default=['_3'])
    parser.add_argument('--users_list', default=['user_1', 'user_3', 'user_4', 'user_5'])
    parser.add_argument('--activities_list', default=['rest', 'biking', 'walking', 'running'])
    parser.add_argument('--samples_list', default=['_0', '_1', '_2', '_3', '_4', '_5', '_6', '_7'])
    parser.add_argument('--test_users_list', default=['user_1', 'user_3', 'user_4', 'user_5'])
    parser.add_argument('--validation_users_list', default=['user_1', 'user_3', 'user_4', 'user_5'])
    parser.add_argument('--validation_samples_list', default=['_0'])
    parser.add_argument('--validation_activities_list', default=['rest', 'biking', 'walking', 'running'])
    parser.add_argument('--test_segment_start', default=60)
    parser.add_argument('--test_segment_end', default=90)
    parser.add_argument('--removed_specific_samples', default=[])

    # SSD目标检测算法参数
    parser.add_argument('--bbox_range', default=2, help='包围盒的默认时间长度，单位s')
    parser.add_argument('--reshape_size', default=(300, 300), help='输入到SSD前，对频谱进行reshape，方便处理 # 在这里reshape size 第一维控制时间，第二维控制freq')
    parser.add_argument('--hr_spectrum_reshape', default=False, help='判断是否使用reshape来处理心率的频谱。用reshape来处理频谱进行映射看看会不会效果更好')

    # nmf process
    parser.add_argument('--is_nmf', default=False, help='是否使用nmf提供额外通道')

    # linear precision
    parser.add_argument('--linear_precision_order', default=30, help='线性预测的阶数')
    parser.add_argument('--lpc_win_len', default=0.4)
    parser.add_argument('--lpc_hop_len', default=0.1)

    # data pre-process pipeline
    parser.add_argument('--audio_pipeline', default='pre_emphasis, filter_data, dwt_filter')
    parser.add_argument('--ecg_pipeline', default='resample_by_scipy_resample, remove_dc_component, filter_data')
    parser.add_argument('--breathing_pipeline', default='resample_by_scipy_resample, remove_dc_component, dwt_filter')
    parser.add_argument('--acc_pipeline', default='resample_by_scipy_resample, remove_dc_component, dwt_filter')
    parser.add_argument('--neck_imu_pipeline', default='None')
    parser.add_argument('--ear_imu_pipeline', default='None')

    # filter setting
    parser.add_argument('--low_cut_freq', default=[0.5, 0.5, 0.3, 0.1, 0.5, 0.5], type=float, help='audio, ecg, breathing, acc')
    parser.add_argument('--high_cut_freq', default=[2, 10, 1, 30, 20, 20], type=float, help='audio, ecg, breathing, acc')
    parser.add_argument('--filter_order', default=[1, 1, 2, 1, 1, 1], type=int, help='audio, ecg, breathing, acc')
    parser.add_argument('--z_score_filter_variance', default=2.5e-6, help='z-score平滑频谱的参数，判断数据是否已经平滑')

    #  EWT setting
    parser.add_argument('--ewt_mel', default=False, help='判断对ewt后的信号做何种stft变换')
    parser.add_argument('--ewt_log_mel', default=False, help='判断对ewt后的信号做何种stft变换')

    # Fourier trans setting
    parser.add_argument('--n_fft', default=[1024, 256, 32, 128, 128, 128])
    parser.add_argument('--hop_length', default=[256, 64, 8, 16, 16, 16])
    parser.add_argument('--win_length', default=[1024, 256, 32, 128, 128, 128])

    # mel trans setting
    parser.add_argument('--n_fft_mel', default=[4096, 128, 25, 128, 128, 128])
    parser.add_argument('--hop_length_mel', default=[1024, 51, 25, 64, 64, 64])
    parser.add_argument('--win_length_mel', default=[4096, 128, 25, 128, 128, 128], help='If unspecified, defaults to ``win_length = n_fft``')
    parser.add_argument('--n_mels', default=[128, 64, 64, 64, 64, 64])
    parser.add_argument('--is_log_mel', default=False, help='是否对mel谱做取对数运算')

    # STFT setting
    parser.add_argument('--n_fft_stft', default=[250, 126, 25, 64, 64, 64], help='原始呼吸stft的三个参数分别是16，8，16，现在改成和ecg一样的')
    parser.add_argument('--hop_length_stft', default=[250, 51, 25, 32, 32, 32], help='If unspecified, defaults to ``win_length // 4`` (see below)')
    parser.add_argument('--win_length_stft', default=[250, 126, 25, 64, 64, 64], help='If unspecified, defaults to ``win_length = n_fft``')
    parser.add_argument('--spectrum_retain_range', default=[[5000, 8000], [0, 50], [0, 64], [], [], []], help='对音频信号的stft频谱进行分割，只保留想要的范围，单位Hz')
    parser.add_argument('--is_power_to_db_stft', default=False, help='是否对短时傅里叶变换使用power to db')

    # wavelet trans setting
    parser.add_argument('--dwt_wavelet_name', default=['sym20', 'db9', 'db9', 'db9', 'db9', 'db9'], type=str, help='wavelet that used for audio, ecg, breathing, acc')
    parser.add_argument('--cwt_wavelet_name', default='mexh', type=str)
    parser.add_argument('--thr_select', default='minimaxi', help='select the threshold type:[sqtwolog, minimaxi, rigrsure, heursure]')
    parser.add_argument('--thr_mode', default='soft', help='select threshold mode:{soft, hard, garrote, greater, less}')
    parser.add_argument('--dwt_remove_level', default=[[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13], [], [1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13], [], [], []])
    parser.add_argument('--dwt_retain_level', default=[[14, 15], [], [8, 9], [], [], []])

    # training setting
    parser.add_argument('--mode', default='Train')
    parser.add_argument('--lr', default=0.002, help='learning rate 0.0001')
    parser.add_argument('--gamma', default=0.25, help='learning rate')
    parser.add_argument('--beta1', default=0.9, help='momentum1 in Adam 0.5 ')
    parser.add_argument('--beta2', default=0.999, help='momentum2 in Adam')
    parser.add_argument('--num_epochs', default=30)
    parser.add_argument('--save_epoch', default=None)
    parser.add_argument('--num_epochs_decay', default=15)
    parser.add_argument('--milestones', default=[40, 60])

    # dataloader setting
    parser.add_argument('--num_workers', default=0)
    parser.add_argument('--batch_size', default=32)

    # cuda setting
    parser.add_argument('--gpu', default=5, help='gup device')
    parser.add_argument('--seed', default=1, help='random seed, default: 1')

    # model settings
    parser.add_argument('--model_folder', default='./log/saved_model/UNet/2024-01-25 10-48-11')
    parser.add_argument('--model_load_pkl', default='./model/01-04-07-38-19 UNet_Pcoding_withoutBN_breathing test-user_1 filter[0.5, 0.5],[10, 10],[1, 1]/01-04-07-38-19 UNet_Pcoding_withoutBN_breathing-best mse-epoch 23-30-lr0.0002-.pkl')
    parser.add_argument('--model_type', default='UNet_Pcoding_withoutBN_breathing', help='what kind of model do you want to use [UNet_Pcoding_withoutBN_breathing]')
    parser.add_argument('--in_channel', default=1)
    parser.add_argument('--out_channel', default=1)
    parser.add_argument('--model_dir', default='./model')
    parser.add_argument('--hidden_size', default=64)
    parser.add_argument('--num_layers', default=2)
    parser.add_argument('--criterion', default='L1Loss')

    # lstm model settings
    parser.add_argument('--lstm_input_size', default=300)
    parser.add_argument('--lstm_hidden_size', default=64)
    parser.add_argument('--lstm_num_layers', default=3)
    parser.add_argument('--lstm_output_size', default=1)
    parser.add_argument('--lstm_bidirectional', default=False)

    # cnn transformer settings
    parser.add_argument('--embed_dim', default=300)
    parser.add_argument('--attention_num_layers', default=4)

    # alignment data setting
    # parser.add_argument('--align_data_dir', default="../datas/zephyr data and record data", type=str)
    parser.add_argument('--align_data_dir', default="/sdf1/zyz/datas/zephyr data and record data", type=str)
    parser.add_argument('--align_activity', default=['biking'])
    parser.add_argument('--align_sample', default=['_0'])
    parser.add_argument('--segment_error', default=0)
    parser.add_argument('--align_audio_start_time', default=2)
    parser.add_argument('--align_audio_end_time', default=7)
    parser.add_argument('--align_ecg_start_time', default=3)
    parser.add_argument('--align_ecg_end_time', default=15)

    # log setting
    parser.add_argument('--result_path', default='./result')

    current_dir = os.path.dirname(os.path.abspath(__file__))
    log_folder_path = os.path.join(current_dir, 'log')
    parser.add_argument('--log_folder', default=log_folder_path)

    parser.add_argument('--log_model_save_folder', default=None)
    parser.add_argument('--log_reconstructed_csv_save_folder', default=None)
    parser.add_argument('--log_fig_save_folder', default=None)
    parser.add_argument('--log_result_folder', default=None)
    parser.add_argument('--run_time_stamp', default=time.strftime("%Y-%m-%d %H-%M-%S"), help='when the program run')
    parser.add_argument('--configuration', default='arg1')
    parser.add_argument('--log_fig_save_path', default='./log_fig', help='用给数据切割时存图片用的不要删除')

    opt = parser.parse_args()
    opt.log_model_save_folder = os.path.join(opt.log_folder, opt.model_type, opt.run_time_stamp + ' ' + opt.configuration, 'saved model')
    opt.log_fig_save_folder = os.path.join(opt.log_folder, opt.model_type, opt.run_time_stamp + ' ' + opt.configuration, 'saved fig')

    return opt


def option_save(opt, save_folder=None):
    if not save_folder:
        setting_folder = os.path.join(opt.log_folder, opt.model_type, opt.run_time_stamp + ' ' + opt.configuration)
    else:
        setting_folder = save_folder
    if not os.path.exists(setting_folder):
        os.makedirs(setting_folder)
    setting_file = os.path.join(setting_folder, 'setting.txt')
    important_setting_file = os.path.join(setting_folder, 'important_setting.txt')
    with open(important_setting_file, 'w') as f:
        f.writelines('model: {}\n'.format(opt.model_type))
        f.writelines('lr: {}\n'.format(opt.lr))
        f.writelines('seed: {}\n'.format(opt.seed))
        f.writelines('beta1: {}\n'.format(opt.beta1))
        f.writelines('beta2: {}\n'.format(opt.beta2))
        f.writelines('batch_size: {}\n'.format(opt.batch_size))
        f.writelines('num_epochs: {}\n'.format(opt.num_epochs))
        f.writelines('window_len: {}\n'.format(opt.window_len))
        f.writelines('window_slide: {}\n\n'.format(opt.window_slide))
        f.writelines('users_list: {}\n'.format(opt.users_list))
        f.writelines('activities_list: {}\n'.format(opt.activities_list))
        f.writelines('samples_list: {}\n'.format(opt.samples_list))

        modality = ['audio', 'ecg', 'breathing', 'acc']
        modality_pipelines = [opt.audio_pipeline, opt.ecg_pipeline, opt.breathing_pipeline, opt.acc_pipeline]
        for i in range(0, len(modality_pipelines)):
            f.writelines('{} pipelines: {}.\n'.format(modality[i], modality_pipelines[i]))
        for i in range(0, len(modality_pipelines)):
            f.writelines('\nmodality' + ' : ' + str(modality[i]) + '\n')
            if 'resample' in modality_pipelines[i]:
                f.writelines('resample rate: {}.\n'.format(opt.re_sr[i]))
            else:
                f.writelines('sample rate: {}.\n'.format(opt.sr[i]))
            if 'filter_data' in modality_pipelines[i]:
                f.writelines('bandpass filter: low_cut {} Hz, high_cut {} Hz, order {}.\n'.format(opt.low_cut_freq[i], opt.high_cut_freq[i], opt.filter_order[i]))
            if 'short_time_ft' in modality_pipelines[i] or 'spectrum' in modality_pipelines[i]:
                f.writelines('stft: n_fft {}, hop_length {}, win_length {}.\n'.format(opt.n_fft_stft[i], opt.hop_length_stft[i], opt.win_length_stft[i]))
            if 'mel' in modality_pipelines[i]:
                f.writelines('mel_transform: n_fft {}, hop_length {}, win_length {}.\n'.format(opt.n_fft_mel[i], opt.hop_length_mel[i], opt.win_length_mel[i]))
            if 'dwt' in modality_pipelines[i]:
                f.writelines('dwt: dwt_retain_level {}.\n'.format(str(opt.dwt_retain_level[i])))
            if 'spectrum_split' in modality_pipelines[i]:
                f.writelines('spectrum_split: spectrum_retain_range {}.\n'.format(str(opt.spectrum_retain_range[i])))
            if 'pre_emphasis' in modality_pipelines[i]:
                f.writelines('pre_emphasis.\n')
            if 'z_score_filter' in modality_pipelines[i]:
                f.writelines('z_score_filter: variance {}.\n'.format(str(opt.z_score_filter_variance)))

    opt_dict = opt.__dict__
    with open(setting_file, 'w') as f:
        for eachArg, value in opt_dict.items():
            f.writelines(eachArg + ' : ' + str(value) + '\n')


args = get_options()
