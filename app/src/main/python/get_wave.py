from config import get_options, option_save
import os
from solver import Solver_total, Solver_APP
import datasets.dataset as dataset
from torch.backends import cudnn
import torch
import numpy as np

app = None  # 定义全局变量

def get_app():
    return app

def total_main(opts, opts_b):
    print(opts.run_time_stamp, opts.configuration)
    print(opts_b.run_time_stamp, opts_b.configuration)
    #cudnn.benchmark = True
    torch.manual_seed(opts.seed)
    np.random.seed(opts.seed)
    opts.result_path = os.path.join(os.getenv('EXTERNAL_STORAGE', '/storage/emulated/0'), 'EPHMonitor', 'result')
    if not os.path.exists(opts.result_path):
        os.makedirs(opts.result_path)

    global app
    app = APP(opts, opts_b)

    for test_user in opts.test_users_list:
        print("test_user")
        print(test_user)
        test_loader = dataset.get_loader_total(opts, opts_b, ['long'], ['long_term'], ['_0'])

        solver = Solver_total(opts, opts_b, test_loader)
        solver.test_total_APP(test_user, app)


class APP:
    def __init__(self, opts, opts_b):
        self.solver = Solver_APP(opts, opts_b)

    def get_predict_ecg_waveform(self, audio):
        predict_ecg_waveform = self.solver.hr_inference(audio)
        print("get ecg_waveform ok !")
        return predict_ecg_waveform

    def get_predict_breathing_waveform(self, audio):
        predict_breathing_waveform = self.solver.br_inference(audio)
        print("get breath_waveform ok !")
        return predict_breathing_waveform

def run():
    # region ---------------------- senet_opts9__ewtv4_inchannel3 ----------------------#

    senet_opts9__ewtv4_inchannel3 = get_options()
    senet_opts9__ewtv4_inchannel3.configuration = 'mel to stft deeper model selected user ewt process v4 leakyrelu in_channel 3 senet wo filterdata'
    senet_opts9__ewtv4_inchannel3.model_type = 'UNet_input_64_99_hr_deeper_senet'
    senet_opts9__ewtv4_inchannel3.run_time_stamp = '2024-07-18 12-07-19'
    # senet_opts9__ewtv4_inchannel3.audio_pipeline = 'resample_by_scipy, inverse2negative, window_cut, ewt_process_stft_v4'
    senet_opts9__ewtv4_inchannel3.audio_pipeline = 'resample_by_scipy, inverse2negative, ewt_process_stft_v4'
    senet_opts9__ewtv4_inchannel3.ecg_pipeline = 'filter_data, resample_by_scipy, remove_dc_component, window_cut, short_time_ft'
    senet_opts9__ewtv4_inchannel3.n_fft_stft = [126, 126, 126, 128]
    senet_opts9__ewtv4_inchannel3.hop_length_stft = [51, 51, 51, 32]
    senet_opts9__ewtv4_inchannel3.win_length_stft = [126, 126, 126, 128]
    senet_opts9__ewtv4_inchannel3.gpu = 'cpu'
    senet_opts9__ewtv4_inchannel3.batch_size = 256
    senet_opts9__ewtv4_inchannel3.num_epochs = 100
    # senet_opts9__ewtv4_inchannel3.save_epoch = 30
    senet_opts9__ewtv4_inchannel3.is_nmf = False
    senet_opts9__ewtv4_inchannel3.ewt_log_mel = True
    senet_opts9__ewtv4_inchannel3.in_channel = 3
    senet_opts9__ewtv4_inchannel3.users_list = ['user_20', 'user_21', 'user_22', 'user_24', 'user_29', 'user_30', 'user_31', 'user_32', 'user_33', 'user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_35', 'user_36']
    senet_opts9__ewtv4_inchannel3.test_users_list = ['long']
    # senet_opts9__ewtv4_inchannel3.users_list = ['user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_17']
    # senet_opts9__ewtv4_inchannel3.test_users_list = ['user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_17']
    # senet_opts9__ewtv4_inchannel3.activities_list = ['biking', 'rest', 'boating', 'running', 'walking',
    #                                                  'biking_earmuffs', 'rest_earmuffs', 'boating_earmuffs', 'running_earmuffs', 'walking_earmuffs']
    senet_opts9__ewtv4_inchannel3.activities_list = ['long_term']
    senet_opts9__ewtv4_inchannel3.window_len = 5
    senet_opts9__ewtv4_inchannel3.window_slide = 2
    senet_opts9__ewtv4_inchannel3.sr = [10000, 250, 25, 100, 1]
    senet_opts9__ewtv4_inchannel3.re_sr = [1000, 1000, 1000, 1000, 1]
    senet_opts9__ewtv4_inchannel3.low_cut_freq = [0.5, 10, 0.3, 0.1]
    senet_opts9__ewtv4_inchannel3.high_cut_freq = [3, 50, 0.7, 30]
    senet_opts9__ewtv4_inchannel3.filter_order = [1, 1, 1, 1]
    # senet_opts9__ewtv4_inchannel3.n_fft_stft = [1024, 1024, 126, 128]
    # senet_opts9__ewtv4_inchannel3.hop_length_stft = [32, 32, 51, 32]
    # senet_opts9__ewtv4_inchannel3.win_length_stft = [1024, 1024, 126, 128]
    senet_opts9__ewtv4_inchannel3.n_fft_mel = [126, 1024, 126, 128]
    senet_opts9__ewtv4_inchannel3.hop_length_mel = [51, 32, 51, 32]
    senet_opts9__ewtv4_inchannel3.win_length_mel = [126, 256, 126, 128]
    senet_opts9__ewtv4_inchannel3.n_mels = [64, 64, 64, 64]
    senet_opts9__ewtv4_inchannel3.breathing_pipeline = 'window_cut'
    senet_opts9__ewtv4_inchannel3.log_model_save_folder = os.path.join(senet_opts9__ewtv4_inchannel3.log_folder, senet_opts9__ewtv4_inchannel3.model_type,
                                                                       senet_opts9__ewtv4_inchannel3.run_time_stamp + ' ' + senet_opts9__ewtv4_inchannel3.configuration, 'saved model')
    senet_opts9__ewtv4_inchannel3.log_reconstructed_csv_save_folder = os.path.join(senet_opts9__ewtv4_inchannel3.log_folder, senet_opts9__ewtv4_inchannel3.model_type,
                                                                                   senet_opts9__ewtv4_inchannel3.run_time_stamp + ' ' + senet_opts9__ewtv4_inchannel3.configuration, 'saved reconstructed csv')
    senet_opts9__ewtv4_inchannel3.log_result_folder = os.path.join(senet_opts9__ewtv4_inchannel3.log_folder, senet_opts9__ewtv4_inchannel3.model_type,
                                                                   senet_opts9__ewtv4_inchannel3.run_time_stamp + ' ' + senet_opts9__ewtv4_inchannel3.configuration, 'saved result')
    senet_opts9__ewtv4_inchannel3.log_fig_save_folder = os.path.join(senet_opts9__ewtv4_inchannel3.log_folder, senet_opts9__ewtv4_inchannel3.model_type,
                                                                     senet_opts9__ewtv4_inchannel3.run_time_stamp + ' ' + senet_opts9__ewtv4_inchannel3.configuration, 'saved fig')
    count = 0
    while os.path.exists(senet_opts9__ewtv4_inchannel3.log_fig_save_folder):
        count += 1
        senet_opts9__ewtv4_inchannel3.log_fig_save_folder = senet_opts9__ewtv4_inchannel3.log_fig_save_folder + str(count)
    count1 = 0
    # while os.path.exists(senet_opts9__ewtv4_inchannel3.log_result_folder):
    #     count1 += 1
    #     senet_opts9__ewtv4_inchannel3.log_result_folder = senet_opts9__ewtv4_inchannel3.log_result_folder + str(count1)
    # endregion

    #  region -------------- opts_mel_DCNv1_2 ----------------#

    opts_mel_DCNv1_2 = get_options()
    opts_mel_DCNv1_2.configuration = 'spectrum2seq breathing selected user mel leakyrelu torchvisionDCNv1 double conv'
    opts_mel_DCNv1_2.model_type = 'DCN_CNN_LSTM_breathing_mel_dcnv1'
    opts_mel_DCNv1_2.run_time_stamp = '2024-07-17 00-42-30'
    opts_mel_DCNv1_2.audio_pipeline = 'filter_data, pre_emphasis, resample_by_scipy, window_cut, log_mel_filter, spectrum_reshape, inverse_freq_time'
    opts_mel_DCNv1_2.ecg_pipeline = 'window_cut'
    opts_mel_DCNv1_2.breathing_pipeline = 'remove_dc_component, filter_data, resample_by_scipy, discrete_wavelet_filter, resample_back_by_scipy, window_cut, normalization'
    opts_mel_DCNv1_2.n_fft_stft = [1024, 1024, 126, 128]
    opts_mel_DCNv1_2.hop_length_stft = [256, 32, 51, 32]
    opts_mel_DCNv1_2.win_length_stft = [1024, 1024, 126, 128]
    opts_mel_DCNv1_2.n_fft_mel = [2048, 1024, 126, 128]
    opts_mel_DCNv1_2.hop_length_mel = [256, 32, 51, 32]
    opts_mel_DCNv1_2.win_length_mel = [2048, 256, 126, 128]
    opts_mel_DCNv1_2.n_mels = [128, 64, 64, 64]
    opts_mel_DCNv1_2.gpu = 'cpu'
    opts_mel_DCNv1_2.batch_size = 64
    opts_mel_DCNv1_2.num_epochs = 50
    opts_mel_DCNv1_2.lr = 0.001
    opts_mel_DCNv1_2.milestones = [15, 30]
    opts_mel_DCNv1_2.ewt_log_mel = True
    # opts_mel_DCNv1_2.save_epoch = 30
    # opts_mel_DCNv1_2.is_nmf = True
    # opts_mel_DCNv1_2.in_channel = 2
    opts_mel_DCNv1_2.lstm_input_size = 512
    opts_mel_DCNv1_2.lstm_hidden_size = 256
    opts_mel_DCNv1_2.lstm_num_layers = 4
    opts_mel_DCNv1_2.lstm_output_size = 1
    opts_mel_DCNv1_2.lstm_bidirectional = True
    opts_mel_DCNv1_2.users_list = ['user_20', 'user_21', 'user_22', 'user_24', 'user_29', 'user_30', 'user_31', 'user_32', 'user_33', 'user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_35', 'user_36']
    opts_mel_DCNv1_2.test_users_list = ['long']
    # opts_mel_DCNv1_2.activities_list = ['biking', 'rest', 'boating', 'running', 'walking',
    #                                     'biking_earmuffs', 'rest_earmuffs', 'boating_earmuffs', 'running_earmuffs', 'walking_earmuffs']
    # opts_mel_DCNv1_2.users_list = ['user_20', 'user_21']
    # opts_mel_DCNv1_2.test_users_list = ['user_20']
    # opts_mel_DCNv1_2.activities_list = ['biking', 'rest', 'boating', 'running', 'walking']
    opts_mel_DCNv1_2.activities_list = ['long_term']
    opts_mel_DCNv1_2.spectrum_retain_range = [[400, 4600], [], [0, 64], []]
    opts_mel_DCNv1_2.dwt_remove_level = [[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13], [], [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], []]
    opts_mel_DCNv1_2.reshape_size = (250, 128)
    opts_mel_DCNv1_2.window_len = 10
    opts_mel_DCNv1_2.window_slide = 3
    opts_mel_DCNv1_2.sr = [10000, 250, 25, 100, 1]
    opts_mel_DCNv1_2.re_sr = [1000, 1000, 1000, 1000, 1]
    opts_mel_DCNv1_2.low_cut_freq = [400, 10, 0.3, 0.1]
    opts_mel_DCNv1_2.high_cut_freq = [4600, 50, 0.8, 30]
    opts_mel_DCNv1_2.filter_order = [1, 1, 1, 1]
    opts_mel_DCNv1_2.log_model_save_folder = os.path.join(opts_mel_DCNv1_2.log_folder, opts_mel_DCNv1_2.model_type, opts_mel_DCNv1_2.run_time_stamp + ' ' + opts_mel_DCNv1_2.configuration, 'saved model')
    opts_mel_DCNv1_2.log_reconstructed_csv_save_folder = os.path.join(opts_mel_DCNv1_2.log_folder, opts_mel_DCNv1_2.model_type, opts_mel_DCNv1_2.run_time_stamp + ' ' + opts_mel_DCNv1_2.configuration, 'saved reconstructed csv')
    opts_mel_DCNv1_2.log_result_folder = os.path.join(opts_mel_DCNv1_2.log_folder, opts_mel_DCNv1_2.model_type, opts_mel_DCNv1_2.run_time_stamp + ' ' + opts_mel_DCNv1_2.configuration, 'saved result')
    opts_mel_DCNv1_2.log_fig_save_folder = os.path.join(opts_mel_DCNv1_2.log_folder, opts_mel_DCNv1_2.model_type, opts_mel_DCNv1_2.run_time_stamp + ' ' + opts_mel_DCNv1_2.configuration, 'saved fig')
    count = 0
    while os.path.exists(opts_mel_DCNv1_2.log_fig_save_folder):
        count += 1
        opts_mel_DCNv1_2.log_fig_save_folder = opts_mel_DCNv1_2.log_fig_save_folder + str(count)
    # count1 = 0
    # while os.path.exists(opts_mel_DCNv1_2.log_result_folder):
    #     count1 += 1
    #     opts_mel_DCNv1_2.log_result_folder = opts_mel_DCNv1_2.log_result_folder + str(count1)
    # endregion

    # total_main(senet_opts9__ewtv4_inchannel3, opts_mel_DCNv1_2)

    # region ---------------------- heart heart_opts senet_opts9__ewtv4_inchannel2 ----------------------#

    heart_opts = get_options()
    heart_opts.configuration = 'mel to stft deeper model selected user ewt process v4 leakyrelu in_channel 2 senet wo filterdata'
    heart_opts.model_type = 'UNet_input_64_99_hr_deeper_senet'
    heart_opts.run_time_stamp = '2024-07-18 12-07-19'
    # heart_opts.audio_pipeline = 'resample_by_scipy, inverse2negative, window_cut, ewt_process_stft_v4'
    heart_opts.audio_pipeline = 'resample_by_scipy, inverse2negative, ewt_process_stft_v4'
    heart_opts.ecg_pipeline = 'filter_data, resample_by_scipy, remove_dc_component, window_cut, short_time_ft'
    heart_opts.n_fft_stft = [126, 126, 126, 128]
    heart_opts.hop_length_stft = [51, 51, 51, 32]
    heart_opts.win_length_stft = [126, 126, 126, 128]
    heart_opts.gpu = 'cpu'
    heart_opts.batch_size = 256
    heart_opts.num_epochs = 100
    # heart_opts.save_epoch = 30
    heart_opts.is_nmf = False
    heart_opts.ewt_log_mel = True
    heart_opts.in_channel = 2
    heart_opts.users_list = ['user_20', 'user_21', 'user_22', 'user_24', 'user_29', 'user_30', 'user_31', 'user_32', 'user_33', 'user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_35', 'user_36']
    # heart_opts.test_users_list = ['user_20', 'user_21', 'user_22', 'user_24', 'user_29', 'user_30', 'user_31', 'user_32', 'user_33', 'user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_35', 'user_36']
    # heart_opts.users_list = ['user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_17']
    heart_opts.test_users_list = ['long']
    # heart_opts.activities_list = ['biking', 'rest', 'boating', 'running', 'walking']
    # heart_opts.activities_list = ['biking', 'rest', 'boating', 'running', 'walking',
    #                                                  'biking_earmuffs', 'rest_earmuffs', 'boating_earmuffs', 'running_earmuffs', 'walking_earmuffs']
    heart_opts.activities_list = ['long_term']
    # heart_opts.samples_list = ['_0', '_3']
    heart_opts.samples_list = ['_0']
    heart_opts.window_len = 5
    heart_opts.window_slide = 2
    heart_opts.sr = [10000, 250, 25, 100, 1]
    heart_opts.re_sr = [1000, 1000, 1000, 1000, 1]
    heart_opts.low_cut_freq = [0.5, 10, 0.3, 0.1]
    heart_opts.high_cut_freq = [3, 50, 0.7, 30]
    heart_opts.filter_order = [1, 1, 1, 1]
    # heart_opts.n_fft_stft = [1024, 1024, 126, 128]
    # heart_opts.hop_length_stft = [32, 32, 51, 32]
    # heart_opts.win_length_stft = [1024, 1024, 126, 128]
    heart_opts.n_fft_mel = [126, 1024, 126, 128]
    heart_opts.hop_length_mel = [51, 32, 51, 32]
    heart_opts.win_length_mel = [126, 256, 126, 128]
    heart_opts.n_mels = [64, 64, 64, 64]
    heart_opts.breathing_pipeline = 'window_cut'
    heart_opts.log_model_save_folder = os.path.join(heart_opts.log_folder, heart_opts.model_type,
                                                    heart_opts.run_time_stamp + ' ' + heart_opts.configuration, 'saved model')
    heart_opts.log_reconstructed_csv_save_folder = os.path.join(heart_opts.log_folder, heart_opts.model_type,
                                                                heart_opts.run_time_stamp + ' ' + heart_opts.configuration, 'saved reconstructed csv')
    heart_opts.log_result_folder = os.path.join(heart_opts.log_folder, heart_opts.model_type,
                                                heart_opts.run_time_stamp + ' ' + heart_opts.configuration, 'saved result')
    heart_opts.log_fig_save_folder = os.path.join(heart_opts.log_folder, heart_opts.model_type,
                                                  heart_opts.run_time_stamp + ' ' + heart_opts.configuration, 'saved fig')
    count = 0
    while os.path.exists(heart_opts.log_fig_save_folder):
        count += 1
        heart_opts.log_fig_save_folder = heart_opts.log_fig_save_folder + str(count)
    count1 = 0
    # while os.path.exists(heart_opts.log_result_folder):
    #     count1 += 1
    #     heart_opts.log_result_folder = heart_opts.log_result_folder + str(count1)
    # endregion

    #  region -------------- breathing opts_mel_DCNv1_2 ----------------#

    breathing_opts = get_options()
    breathing_opts.configuration = 'spectrum2seq breathing selected user mel leakyrelu torchvisionDCNv1 double conv'
    breathing_opts.model_type = 'DCN_CNN_LSTM_breathing_mel_dcnv1'
    breathing_opts.run_time_stamp = '2024-07-17 00-42-30'
    # breathing_opts.audio_pipeline = 'filter_data, pre_emphasis, resample_by_scipy, window_cut, log_mel_filter, spectrum_reshape, inverse_freq_time'
    breathing_opts.audio_pipeline = 'filter_data, pre_emphasis, resample_by_scipy, log_mel_filter, spectrum_reshape, inverse_freq_time'
    breathing_opts.ecg_pipeline = 'window_cut'
    breathing_opts.breathing_pipeline = 'remove_dc_component, filter_data, resample_by_scipy, discrete_wavelet_filter, resample_back_by_scipy, window_cut, normalization'
    breathing_opts.n_fft_stft = [1024, 1024, 126, 128]
    breathing_opts.hop_length_stft = [256, 32, 51, 32]
    breathing_opts.win_length_stft = [1024, 1024, 126, 128]
    breathing_opts.n_fft_mel = [2048, 1024, 126, 128]
    breathing_opts.hop_length_mel = [256, 32, 51, 32]
    breathing_opts.win_length_mel = [2048, 256, 126, 128]
    breathing_opts.n_mels = [128, 64, 64, 64]
    breathing_opts.gpu = 'cpu'
    breathing_opts.batch_size = 64
    breathing_opts.num_epochs = 50
    breathing_opts.lr = 0.001
    breathing_opts.milestones = [15, 30]
    # breathing_opts.save_epoch = 30
    # breathing_opts.is_nmf = True
    # breathing_opts.in_channel = 2
    breathing_opts.lstm_input_size = 512
    breathing_opts.lstm_hidden_size = 256
    breathing_opts.lstm_num_layers = 4
    breathing_opts.lstm_output_size = 1
    breathing_opts.lstm_bidirectional = True
    breathing_opts.users_list = ['user_20', 'user_21', 'user_22', 'user_24', 'user_29', 'user_30', 'user_31', 'user_32', 'user_33', 'user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_35', 'user_36']
    breathing_opts.test_users_list = ['user_20', 'user_21', 'user_22', 'user_24', 'user_29', 'user_30', 'user_31', 'user_32', 'user_33', 'user_10', 'user_11', 'user_13', 'user_14', 'user_15', 'user_35', 'user_36']
    # breathing_opts.activities_list = ['biking', 'rest', 'boating', 'running', 'walking',
    #                                     'biking_earmuffs', 'rest_earmuffs', 'boating_earmuffs', 'running_earmuffs', 'walking_earmuffs']
    # breathing_opts.users_list = ['user_20', 'user_21']
    # breathing_opts.test_users_list = ['user_20']
    breathing_opts.activities_list = ['biking', 'rest', 'boating', 'running', 'walking']
    breathing_opts.spectrum_retain_range = [[400, 4600], [], [0, 64], []]
    breathing_opts.dwt_remove_level = [[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13], [], [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], []]
    breathing_opts.reshape_size = (250, 128)
    breathing_opts.window_len = 10
    breathing_opts.window_slide = 3
    breathing_opts.sr = [10000, 250, 25, 100, 1]
    breathing_opts.re_sr = [10000, 1000, 1000, 1000, 1]
    breathing_opts.low_cut_freq = [400, 10, 0.3, 0.1]
    breathing_opts.high_cut_freq = [4600, 50, 0.8, 30]
    breathing_opts.filter_order = [1, 1, 1, 1]
    breathing_opts.log_model_save_folder = os.path.join(breathing_opts.log_folder, breathing_opts.model_type, breathing_opts.run_time_stamp + ' ' + breathing_opts.configuration, 'saved model')
    breathing_opts.log_reconstructed_csv_save_folder = os.path.join(breathing_opts.log_folder, breathing_opts.model_type, breathing_opts.run_time_stamp + ' ' + breathing_opts.configuration, 'saved reconstructed csv')
    breathing_opts.log_result_folder = os.path.join(breathing_opts.log_folder, breathing_opts.model_type, breathing_opts.run_time_stamp + ' ' + breathing_opts.configuration, 'saved result')
    breathing_opts.log_fig_save_folder = os.path.join(breathing_opts.log_folder, breathing_opts.model_type, breathing_opts.run_time_stamp + ' ' + breathing_opts.configuration, 'saved fig')
    count = 0
    while os.path.exists(breathing_opts.log_fig_save_folder):
        count += 1
        breathing_opts.log_fig_save_folder = breathing_opts.log_fig_save_folder + str(count)
    # count1 = 0
    # while os.path.exists(breathing_opts.log_result_folder):
    #     count1 += 1
    #     breathing_opts.log_result_folder = breathing_opts.log_result_folder + str(count1)
    # endregion

    # total_main(heart_opts, breathing_opts)
    total_main(heart_opts, breathing_opts)

if __name__ == '__main__':
    run()

